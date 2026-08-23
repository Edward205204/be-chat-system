package com.edward.chat_system.features.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.file.FileService;
import com.edward.chat_system.features.permission.repository.RoleMemberRepository;
import com.edward.chat_system.features.permission.service.RoleService;
import com.edward.chat_system.features.server.dto.request.BanMemberRequest;
import com.edward.chat_system.features.server.dto.request.CreateServerRequest;
import com.edward.chat_system.features.server.dto.request.MuteMemberRequest;
import com.edward.chat_system.features.server.dto.request.ResponseDirectInvite;
import com.edward.chat_system.features.server.dto.response.ServerResponse;
import com.edward.chat_system.features.server.dto.response.UserJoinServerByLinkResponse;
import com.edward.chat_system.features.server.entity.InviteLink;
import com.edward.chat_system.features.server.entity.Server;
import com.edward.chat_system.features.server.entity.ServerInvitation;
import com.edward.chat_system.features.server.entity.ServerMember;
import com.edward.chat_system.features.server.enums.InviteAction;
import com.edward.chat_system.features.server.enums.InviteStatusEnum;
import com.edward.chat_system.features.server.mapper.InviteEnumMapper;
import com.edward.chat_system.features.server.mapper.InviteLinkMapper;
import com.edward.chat_system.features.server.mapper.ServerMapper;
import com.edward.chat_system.features.server.projection.ServerProjection;
import com.edward.chat_system.features.server.repository.*;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.mapper.UserMapper;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.edward.chat_system.shared.utils.CursorUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock ServerRepository serverRepository;
    @Mock ServerInvitationRepository serverInvitationRepository;
    @Mock UserRepository userRepository;
    @Mock ServerMemberRepository serverMemberRepository;
    @Mock RoleService roleService;
    @Mock ChannelRepository channelRepository;
    @Mock ServerMapper serverMapper;
    @Mock RoleMemberRepository roleMemberRepository;
    @Mock ServerBanRepository serverBanRepository;
    @Mock UserMapper userMapper;
    @Mock InviteLinkRepository inviteLinkRepository;
    @Mock CursorUtils cursorUtils;
    @Mock InviteLinkMapper inviteLinkMapper;
    @Mock FileService fileService;
    @Mock InviteEnumMapper inviteEnumMapper;

    @InjectMocks ServerService serverService;

    private ServerProjection mockProjection(String ownerId, String userId) {
        return new ServerProjection() {
            @Override public String getServerId() { return "srv-1"; }
            @Override public String getName() { return "Test Server"; }
            @Override public String getAvatar() { return null; }
            @Override public String getBanner() { return null; }
            @Override public String getOwnerId() { return ownerId; }
            @Override public java.time.LocalDateTime getJoinedAt() { return LocalDateTime.now(); }
        };
    }

    @Test
    void getMyServers_returnsServerList() {
        ServerProjection proj = new ServerProjection() {
            @Override public String getServerId() { return "srv-1"; }
            @Override public String getName() { return "My Server"; }
            @Override public String getAvatar() { return null; }
            @Override public String getBanner() { return null; }
            @Override public String getOwnerId() { return "user-1"; }
            @Override public LocalDateTime getJoinedAt() { return LocalDateTime.now(); }
        };

        when(serverRepository.findAllServerUserJoined("user-1")).thenReturn(List.of(proj));

        List<ServerResponse> result = serverService.getMyServers("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("srv-1");
        assertThat(result.get(0).isOwner()).isTrue();
    }

    @Test
    void getServerById_memberExists_returnsServerResponse() {
        ServerProjection projection = mockProjection("user-1", "user-1");
        when(serverRepository.findServerUserJoinedByServerIdAndUserId("srv-1", "user-1"))
                .thenReturn(Optional.of(projection));

        ServerResponse result = serverService.getServerById("srv-1", "user-1");

        assertThat(result.getId()).isEqualTo("srv-1");
        assertThat(result.isOwner()).isTrue();
    }

    @Test
    void getServerById_notMember_throwsNotAMember() {
        when(serverRepository.findServerUserJoinedByServerIdAndUserId("srv-1", "user-2"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> serverService.getServerById("srv-1", "user-2"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }

    @Test
    void createServer_nameNotDuplicate_createsServerWithDefaultsAndReturns() {
        CreateServerRequest request = new CreateServerRequest();
        request.setName("NewServer");
        request.setAvatar("avatar-id");

        User user = User.builder().id("user-1").build();
        Server savedServer = Server.builder().id("srv-1").name("NewServer").build();
        ServerMember savedMember = ServerMember.builder().server(savedServer).user(user)
                .joinedAt(LocalDateTime.now()).build();

        when(serverRepository.existsByUserIdAndName("user-1", "NewServer")).thenReturn(false);
        when(userRepository.getReferenceById("user-1")).thenReturn(user);
        when(serverRepository.save(any())).thenReturn(savedServer);
        when(serverMemberRepository.save(any())).thenReturn(savedMember);
        when(channelRepository.save(any())).thenReturn(null);

        ServerResponse result = serverService.createServer("user-1", request);

        assertThat(result.getId()).isEqualTo("srv-1");
        assertThat(result.isOwner()).isTrue();
        verify(roleService).createDefaultEveryoneRoleWithDefaultPermission("srv-1");
        verify(fileService).claimFile("avatar-id");
    }

    @Test
    void createServer_nameDuplicate_throwsServerNameDuplicate() {
        CreateServerRequest request = new CreateServerRequest();
        request.setName("Existing");

        when(serverRepository.existsByUserIdAndName("user-1", "Existing")).thenReturn(true);

        assertThatThrownBy(() -> serverService.createServer("user-1", request))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.SERVER_NAME_DUPLICATE));
    }

    @Test
    void deleteServer_delegatesToRepository() {
        serverService.deleteServer("srv-1");
        verify(serverRepository).deleteById("srv-1");
    }

    @Test
    void kickMember_delegatesToRepository() {
        serverService.kickMember("srv-1", "m1");
        verify(serverMemberRepository).deleteByIdAndServerId("m1", "srv-1");
    }

    @Test
    void leaveServer_delegatesToRepository() {
        serverService.leaveServer("srv-1", "user-1");
        verify(serverMemberRepository).deleteByServerIdAndUserId("srv-1", "user-1");
    }

    @Test
    void transferOwner_memberFound_updatesOwner() {
        ServerMember newOwnerMember = ServerMember.builder().id("m-2")
                .user(User.builder().id("user-2").build()).build();

        when(serverMemberRepository.findByIdAndServerId("m-2", "srv-1"))
                .thenReturn(Optional.of(newOwnerMember));

        serverService.transferOwner("srv-1", "m-2");

        verify(serverRepository).updateOwner("srv-1", "user-2");
    }

    @Test
    void transferOwner_memberNotFound_throwsNotAMember() {
        when(serverMemberRepository.findByIdAndServerId("m-99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serverService.transferOwner("srv-1", "m-99"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }

    @Test
    void muteMember_delegatesToRepository() {
        MuteMemberRequest request = new MuteMemberRequest();
        request.setMute(true);

        serverService.muteMember("srv-1", "m1", request);

        verify(serverMemberRepository).muteOrUnmuteServerMember("srv-1", "m1", true);
    }

    @Test
    void banMember_validInput_removesAndBansUser() {
        BanMemberRequest request = new BanMemberRequest();
        request.setReason("spam");

        when(serverMemberRepository.existsByServerIdAndUserId("srv-1", "banned-user")).thenReturn(true);
        when(serverRepository.getReferenceById("srv-1")).thenReturn(Server.builder().id("srv-1").build());
        when(userRepository.getReferenceById("banned-user")).thenReturn(User.builder().id("banned-user").build());
        when(userRepository.getReferenceById("banner-user")).thenReturn(User.builder().id("banner-user").build());

        serverService.banMember("srv-1", "banned-user", "banner-user", request);

        verify(serverMemberRepository).deleteByServerIdAndUserId("srv-1", "banned-user");
        verify(serverBanRepository).save(any());
    }

    @Test
    void banMember_userNotMember_throwsNotAMember() {
        when(serverMemberRepository.existsByServerIdAndUserId("srv-1", "non-member")).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                serverService.banMember(
                                        "srv-1", "non-member", "banner-user", new BanMemberRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }

    @Test
    void unbanMember_delegatesToRepository() {
        serverService.unbanMember("srv-1", "user-1");
        verify(serverBanRepository).deleteByServerIdAndUserId("srv-1", "user-1");
    }

    @Test
    void joinByLink_validToken_joinsMemberAndIncrementsCount() {
        Server server = Server.builder().id("srv-1").name("TestSrv").build();
        InviteLink inviteLink = InviteLink.builder().server(server).token("tok").useCount(0).build();
        ServerMember member = ServerMember.builder().joinedAt(LocalDateTime.now()).build();

        when(inviteLinkRepository.findValidInviteLinkByToken("tok")).thenReturn(Optional.of(inviteLink));
        when(serverMemberRepository.existsByServerIdAndUserId("srv-1", "user-1")).thenReturn(false);
        when(serverBanRepository.existsByServer_IdAndUser_Id("srv-1", "user-1")).thenReturn(false);
        when(serverRepository.getReferenceById("srv-1")).thenReturn(server);
        when(userRepository.getReferenceById("user-1")).thenReturn(User.builder().id("user-1").build());
        when(serverMemberRepository.save(any())).thenReturn(member);
        when(inviteLinkRepository.save(inviteLink)).thenReturn(inviteLink);

        UserJoinServerByLinkResponse result = serverService.jointByLink("tok", "user-1");

        assertThat(result.getServerId()).isEqualTo("srv-1");
        assertThat(inviteLink.getUseCount()).isEqualTo(1);
    }

    @Test
    void joinByLink_invalidToken_throwsInviteLinkNotFound() {
        when(inviteLinkRepository.findValidInviteLinkByToken("bad-tok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serverService.jointByLink("bad-tok", "user-1"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.INVITE_LINK_NOT_FOUND));
    }

    @Test
    void joinByLink_userAlreadyMember_throwsAlreadyMember() {
        Server server = Server.builder().id("srv-1").build();
        InviteLink inviteLink = InviteLink.builder().server(server).token("tok").useCount(0).build();

        when(inviteLinkRepository.findValidInviteLinkByToken("tok")).thenReturn(Optional.of(inviteLink));
        when(serverMemberRepository.existsByServerIdAndUserId("srv-1", "user-1")).thenReturn(true);

        assertThatThrownBy(() -> serverService.jointByLink("tok", "user-1"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.USER_ALREADY_A_MEMBER));
    }

    @Test
    void joinByLink_userBanned_throwsUserBanned() {
        Server server = Server.builder().id("srv-1").build();
        InviteLink inviteLink = InviteLink.builder().server(server).token("tok").useCount(0).build();

        when(inviteLinkRepository.findValidInviteLinkByToken("tok")).thenReturn(Optional.of(inviteLink));
        when(serverMemberRepository.existsByServerIdAndUserId("srv-1", "user-1")).thenReturn(false);
        when(serverBanRepository.existsByServer_IdAndUser_Id("srv-1", "user-1")).thenReturn(true);

        assertThatThrownBy(() -> serverService.jointByLink("tok", "user-1"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.USER_BANNED));
    }

    @Test
    void responseToServerDirectInvite_accept_joinsServer() {
        Server server = Server.builder().id("srv-1").build();
        ServerInvitation invitation = new ServerInvitation();
        invitation.setServer(server);
        invitation.setStatus(InviteStatusEnum.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));

        ResponseDirectInvite request = new ResponseDirectInvite();
        request.setAction(InviteAction.ACCEPT);

        when(serverInvitationRepository.findByIdAndInviteeId("inv-1", "user-1"))
                .thenReturn(Optional.of(invitation));
        when(inviteEnumMapper.map(InviteAction.ACCEPT)).thenReturn(InviteStatusEnum.ACCEPTED);
        when(serverMemberRepository.existsByServerIdAndUserId("srv-1", "user-1")).thenReturn(false);
        when(serverBanRepository.existsByServer_IdAndUser_Id("srv-1", "user-1")).thenReturn(false);
        when(serverRepository.getReferenceById("srv-1")).thenReturn(server);
        when(userRepository.getReferenceById("user-1")).thenReturn(User.builder().id("user-1").build());
        when(serverMemberRepository.save(any())).thenReturn(ServerMember.builder().build());

        serverService.responseToServerDirectInvite("user-1", "inv-1", request);

        verify(serverMemberRepository).save(any());
        verify(serverInvitationRepository)
                .updateAllPendingByInviteeIdAndServerId(InviteStatusEnum.ACCEPTED, "user-1", "srv-1");
    }

    @Test
    void responseToServerDirectInvite_inviteNotFound_throwsNotFoundOrExpired() {
        when(serverInvitationRepository.findByIdAndInviteeId("inv-99", "user-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                serverService.responseToServerDirectInvite(
                                        "user-1", "inv-99", new ResponseDirectInvite()))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.DIRECT_INVITE_NOT_FOUND_OR_EXPIRATED));
    }

    @Test
    void responseToServerDirectInvite_expiredInvite_throwsNotFoundOrExpired() {
        ServerInvitation invitation = new ServerInvitation();
        invitation.setStatus(InviteStatusEnum.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(serverInvitationRepository.findByIdAndInviteeId("inv-1", "user-1"))
                .thenReturn(Optional.of(invitation));

        assertThatThrownBy(
                        () ->
                                serverService.responseToServerDirectInvite(
                                        "user-1", "inv-1", new ResponseDirectInvite()))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.DIRECT_INVITE_NOT_FOUND_OR_EXPIRATED));
    }

    @Test
    void responseToServerDirectInvite_alreadyUsed_throwsDirectInviteHasUsed() {
        ServerInvitation invitation = new ServerInvitation();
        invitation.setStatus(InviteStatusEnum.ACCEPTED);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(serverInvitationRepository.findByIdAndInviteeId("inv-1", "user-1"))
                .thenReturn(Optional.of(invitation));

        assertThatThrownBy(
                        () ->
                                serverService.responseToServerDirectInvite(
                                        "user-1", "inv-1", new ResponseDirectInvite()))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.DIRECT_INVITE_HAS_USED));
    }

    @Test
    void cancelDirectInvite_pendingInvite_deletesIt() {
        ServerInvitation invitation = new ServerInvitation();
        invitation.setStatus(InviteStatusEnum.PENDING);

        when(serverInvitationRepository.findByIdAndServerIdAndInviterId("inv-1", "srv-1", "user-1"))
                .thenReturn(Optional.of(invitation));

        serverService.cancelDirectInvite("user-1", "srv-1", "inv-1");

        verify(serverInvitationRepository).deleteDirectInviteById("inv-1");
    }

    @Test
    void cancelDirectInvite_notFound_throwsCannotCancel() {
        when(serverInvitationRepository.findByIdAndServerIdAndInviterId("inv-99", "srv-1", "user-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> serverService.cancelDirectInvite("user-1", "srv-1", "inv-99"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.CANNOT_CANCEL_INVITE));
    }

    @Test
    void cancelDirectInvite_alreadyUsed_throwsCannotCancel() {
        ServerInvitation invitation = new ServerInvitation();
        invitation.setStatus(InviteStatusEnum.ACCEPTED);

        when(serverInvitationRepository.findByIdAndServerIdAndInviterId("inv-1", "srv-1", "user-1"))
                .thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> serverService.cancelDirectInvite("user-1", "srv-1", "inv-1"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.CANNOT_CANCEL_INVITE));
    }
}
