package com.edward.chat_system.infrastructure.security.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.channel.repository.ChannelRolePermissionRepository;
import com.edward.chat_system.features.channel.repository.ChannelUserPermissionRepository;
import com.edward.chat_system.features.server.projection.ServerMemberInfoProjection;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequiresChannelPermissionComponentTest {

    @Mock CurrentUserProvider currentUserProvider;
    @Mock ChannelRepository channelRepository;
    @Mock ServerMemberRepository serverMemberRepository;
    @Mock ChannelUserPermissionRepository channelUserPermissionRepository;
    @Mock ChannelRolePermissionRepository channelRolePermissionRepository;

    @InjectMocks RequiresChannelPermissionComponent channelPermissionComponent;

    private ServerMemberInfoProjection memberInfo(String userId, boolean isOwner) {
        return new ServerMemberInfoProjection() {
            @Override public String getUserId() { return userId; }
            @Override public String getServerId() { return "srv-1"; }
            @Override public boolean getIsOwner() { return isOwner; }
        };
    }

    private Channel publicChannel(String id) {
        Channel ch = Channel.builder().id(id).build();
        ch.setPrivate(false);
        return ch;
    }

    private Channel privateChannel(String id) {
        Channel ch = Channel.builder().id(id).build();
        ch.setPrivate(true);
        return ch;
    }

    @Test
    void resolveServerId_channelExists_returnsServerId() {
        when(channelRepository.findServerIdByChannelId("ch-1")).thenReturn(Optional.of("srv-1"));

        String result = channelPermissionComponent.resolveServerId("ch-1");

        assertThat(result).isEqualTo("srv-1");
    }

    @Test
    void resolveServerId_channelNotExist_throwsChannelNotExist() {
        when(channelRepository.findServerIdByChannelId("ch-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> channelPermissionComponent.resolveServerId("ch-99"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.CHANNEL_IS_NOT_EXIST));
    }

    @Test
    void check_ownerUser_passesAllPermissions() {
        when(currentUserProvider.getUserId()).thenReturn("owner");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "owner"))
                .thenReturn(Optional.of(memberInfo("owner", true)));

        assertThatCode(
                        () ->
                                channelPermissionComponent.check(
                                        "srv-1", "ch-1", ChannelPermissionKeyEnum.MANAGE_CHANNEL_PERMISSIONS))
                .doesNotThrowAnyException();

        verifyNoInteractions(channelRepository);
    }

    @Test
    void check_nonePermission_passesWithoutChannelLookup() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1"))
                .thenReturn(Optional.of(memberInfo("user-1", false)));

        assertThatCode(
                        () ->
                                channelPermissionComponent.check(
                                        "srv-1", "ch-1", ChannelPermissionKeyEnum.NONE))
                .doesNotThrowAnyException();

        verifyNoInteractions(channelRepository);
    }

    @Test
    void check_publicChannel_passesWithoutPermissionLookup() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1"))
                .thenReturn(Optional.of(memberInfo("user-1", false)));
        when(channelRepository.findById("ch-1")).thenReturn(Optional.of(publicChannel("ch-1")));

        assertThatCode(
                        () ->
                                channelPermissionComponent.check(
                                        "srv-1", "ch-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .doesNotThrowAnyException();

        verifyNoInteractions(channelUserPermissionRepository);
        verifyNoInteractions(channelRolePermissionRepository);
    }

    @Test
    void check_privateChannel_userHasDirectPermission_passes() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1"))
                .thenReturn(Optional.of(memberInfo("user-1", false)));
        when(channelRepository.findById("ch-1")).thenReturn(Optional.of(privateChannel("ch-1")));
        when(channelUserPermissionRepository.hasPermission("user-1", "ch-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(true);

        assertThatCode(
                        () ->
                                channelPermissionComponent.check(
                                        "srv-1", "ch-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .doesNotThrowAnyException();
    }

    @Test
    void check_privateChannel_userHasRolePermission_passes() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1"))
                .thenReturn(Optional.of(memberInfo("user-1", false)));
        when(channelRepository.findById("ch-1")).thenReturn(Optional.of(privateChannel("ch-1")));
        when(channelUserPermissionRepository.hasPermission("user-1", "ch-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(false);
        when(channelRolePermissionRepository.hasPermission("user-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(true);

        assertThatCode(
                        () ->
                                channelPermissionComponent.check(
                                        "srv-1", "ch-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .doesNotThrowAnyException();
    }

    @Test
    void check_privateChannel_noPermission_throwsMissingPermission() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1"))
                .thenReturn(Optional.of(memberInfo("user-1", false)));
        when(channelRepository.findById("ch-1")).thenReturn(Optional.of(privateChannel("ch-1")));
        when(channelUserPermissionRepository.hasPermission("user-1", "ch-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(false);
        when(channelRolePermissionRepository.hasPermission("user-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(false);

        assertThatThrownBy(
                        () ->
                                channelPermissionComponent.check(
                                        "srv-1", "ch-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.MISSING_PERMISSION));
    }

    @Test
    void check_notAMember_throwsNotAMember() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                channelPermissionComponent.check(
                                        "srv-1", "ch-1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }

    @Test
    void check_withExplicitUserId_ownerPasses() {
        when(serverMemberRepository.findServerMemberInfo("srv-1", "explicit-owner"))
                .thenReturn(Optional.of(memberInfo("explicit-owner", true)));

        assertThatCode(
                        () ->
                                channelPermissionComponent.check(
                                        "explicit-owner",
                                        "srv-1",
                                        "ch-1",
                                        ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .doesNotThrowAnyException();
    }
}
