package com.edward.chat_system.features.server.controller;

import com.edward.chat_system.features.channel.dto.request.AddToChannelRequest;
import com.edward.chat_system.features.channel.dto.request.ChannelPatchUpdateRequest;
import com.edward.chat_system.features.channel.dto.request.CreateChannelRequest;
import com.edward.chat_system.features.channel.dto.response.ChannelResponse;
import com.edward.chat_system.features.channel.service.ChannelService;
import com.edward.chat_system.features.server.dto.request.BanMemberRequest;
import com.edward.chat_system.features.server.dto.request.CreateServerRequest;
import com.edward.chat_system.features.server.dto.request.MuteMemberRequest;
import com.edward.chat_system.features.server.dto.request.ServerPatchUpdateRequest;
import com.edward.chat_system.features.server.dto.request.TransferOwnerRequest;
import com.edward.chat_system.features.server.dto.response.*;
import com.edward.chat_system.features.server.service.ServerService;
import com.edward.chat_system.shared.dto.ApiResponse;
import com.edward.chat_system.shared.dto.CursorPageResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ServerController {

    ServerService serverService;
    ChannelService channelService;

    // ─── 1. Server CRUD ──────────────────────────────────────────────────────

    @GetMapping
    ApiResponse<List<ServerResponse>> getMyServers(@AuthenticationPrincipal Jwt principal) {
        return ApiResponse.<List<ServerResponse>>builder()
                .message("Get servers successfully")
                .result(serverService.getMyServers(principal.getSubject()))
                .build();
    }

    @PostMapping
    ApiResponse<ServerResponse> createServer(
            @AuthenticationPrincipal Jwt principal,
            @RequestBody @Valid CreateServerRequest request) {
        return ApiResponse.<ServerResponse>builder()
                .message("Create server successfully")
                .result(serverService.createServer(principal.getSubject(), request))
                .build();
    }

    // AFTER
    @GetMapping("/{serverId}")
    ApiResponse<ServerResponse> getServerById(
            @AuthenticationPrincipal Jwt principal, @PathVariable String serverId) {
        return ApiResponse.<ServerResponse>builder()
                .message("Get server successfully")
                .result(serverService.getServerById(serverId, principal.getSubject()))
                .build();
    }

    // AFTER
    @PatchMapping("/{serverId}")
    ApiResponse<ServerUpdateResponse> updateServer(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @RequestBody @Valid ServerPatchUpdateRequest request) {
        return ApiResponse.<ServerUpdateResponse>builder()
                .message("Update server successfully")
                .result(serverService.serverUpdateResponse(serverId, principal.getSubject(), request))
                .build();
    }

    // AFTER
    @DeleteMapping("/{serverId}")
    ApiResponse<Void> deleteServer(
            @AuthenticationPrincipal Jwt principal, @PathVariable String serverId) {
        serverService.deleteServer(serverId);
        return ApiResponse.<Void>builder().message("Delete server successfully").build();
    }

    // ─── 2. Server Members ────────────────────────────────────────────────────

    // AFTER
    @GetMapping("/{serverId}/members")
    ApiResponse<ServerMemberResponse> getServerMembers(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ApiResponse.<ServerMemberResponse>builder()
                .message("Get members successfully")
                .result(serverService.getServerMember(serverId, pageable))
                .build();
    }

    // AFTER
    @DeleteMapping("/{serverId}/members/{memberId}")
    ApiResponse<Void> kickMember(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PathVariable String memberId) {
        serverService.kickMember(serverId, memberId);
        return ApiResponse.<Void>builder().message("Kick member successfully").build();
    }

    // AFTER
    @PatchMapping("/{serverId}/owner")
    ApiResponse<Void> transferOwner(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @RequestBody @Valid TransferOwnerRequest request) {
        serverService.transferOwner(serverId, request.getMemberId());
        return ApiResponse.<Void>builder().message("Transfer ownership successfully").build();
    }

    // AFTER
    @DeleteMapping("/{serverId}/members/me")
    ApiResponse<Void> leaveServer(
            @AuthenticationPrincipal Jwt principal, @PathVariable String serverId) {
        serverService.leaveServer(serverId, principal.getSubject());
        return ApiResponse.<Void>builder().message("Leave server successfully").build();
    }

    // AFTER
    @PatchMapping("/{serverId}/members/{memberId}/mute")
    ApiResponse<Void> muteMember(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PathVariable String memberId,
            @RequestBody @Valid MuteMemberRequest request) {
        serverService.muteMember(serverId, memberId, request);
        return ApiResponse.<Void>builder().message("Mute member successfully").build();
    }

    // ─── 3. Ban ───────────────────────────────────────────────────────────────

    // AFTER
    @PostMapping("/{serverId}/bans/{userId}")
    ApiResponse<Void> banMember(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PathVariable String userId,
            @RequestBody @Valid BanMemberRequest request) {
        serverService.banMember(serverId, userId, principal.getSubject(), request);
        return ApiResponse.<Void>builder().message("Ban user successfully").build();
    }

    // AFTER
    @DeleteMapping("/{serverId}/bans/{userId}")
    ApiResponse<Void> unbanMember(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PathVariable String userId) {
        serverService.unbanMember(serverId, userId);
        return ApiResponse.<Void>builder().message("Unban user successfully").build();
    }

    // AFTER
    @GetMapping("/{serverId}/bans")
    ApiResponse<List<ServerBanResponse>> getBanList(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ApiResponse.<List<ServerBanResponse>>builder()
                .message("Get ban list successfully")
                .result(serverService.banList(serverId, pageable))
                .build();
    }

    // ─── 4. Invite Link ───────────────────────────────────────────────────────

    // AFTER
    @PostMapping("/{serverId}/links")
    ApiResponse<InviteLinkResponse> createInviteLink(
            @AuthenticationPrincipal Jwt principal, @PathVariable String serverId) {
        return ApiResponse.<InviteLinkResponse>builder()
                .message("Create invite link successfully")
                .result(serverService.createInviteLink(serverId, principal.getSubject()))
                .build();
    }

    // AFTER
    @GetMapping("/{serverId}/links")
    ApiResponse<CursorPageResponse<InviteLinkResponse>> getAllInviteLinks(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorPageResponse<InviteLinkResponse>>builder()
                .message("Get invite links successfully")
                .result(serverService.getAllInviteLink(serverId, cursor, size))
                .build();
    }

    // AFTER
    @PatchMapping("/{serverId}/links/{linkId}/revoke")
    ApiResponse<Void> revokeInviteLink(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PathVariable String linkId) {
        serverService.revokeInviteLink(serverId, linkId);
        return ApiResponse.<Void>builder().message("Revoke invite link successfully").build();
    }

    // ─── 4.4 Join by Invite Link ──────────────────────────────────────────────

    // AFTER
    @PostMapping("/join/{token}")
    ApiResponse<UserJoinServerByLinkResponse> joinByLink(
            @AuthenticationPrincipal Jwt principal, @PathVariable String token) {
        return ApiResponse.<UserJoinServerByLinkResponse>builder()
                .message("Join server successfully")
                .result(serverService.jointByLink(token, principal.getSubject()))
                .build();
    }

    // ─── 5. Direct Invitation ─────────────────────────────────────────────────

    //AFTER: Endpoint 5.1 — POST /{serverId}/invitations (CREATE_INVITE)
    //AFTER: Endpoint 5.2 — GET /invitations (get received invitations)
    //AFTER: Endpoint 5.3 — PATCH /invitations/{invitationId} (accept/reject)
    //AFTER: Endpoint 5.4 — DELETE /{serverId}/invitations/{invitationId} (cancel)

    // ─── 6. Channel ────────────────────────────────────────────────────────────

    // AFTER
    @GetMapping("/{serverId}/channels")
    ApiResponse<CursorPageResponse<ChannelResponse>> getChannels(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.<CursorPageResponse<ChannelResponse>>builder()
                .message("Get channels successfully")
                .result(channelService.getChannelList(serverId, principal.getSubject(), cursor, size))
                .build();
    }

    // AFTER
    @PostMapping("/{serverId}/channels")
    ApiResponse<ChannelResponse> createChannel(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @RequestBody @Valid CreateChannelRequest request) {
        return ApiResponse.<ChannelResponse>builder()
                .message("Create channel successfully")
                .result(channelService.createChannel(serverId, request))
                .build();
    }

    // AFTER
    @PatchMapping("/{serverId}/channels/{channelId}")
    ApiResponse<ChannelResponse> updateChannel(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PathVariable String channelId,
            @RequestBody @Valid ChannelPatchUpdateRequest request) {
        return ApiResponse.<ChannelResponse>builder()
                .message("Update channel successfully")
                .result(channelService.channelPatchUpdate(serverId, channelId, request))
                .build();
    }

    // AFTER
    @DeleteMapping("/{serverId}/channels/{channelId}")
    ApiResponse<Void> deleteChannel(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PathVariable String channelId) {
        channelService.deleteChannel(serverId, channelId);
        return ApiResponse.<Void>builder().message("Delete channel successfully").build();
    }

    // AFTER
    @PostMapping("/{serverId}/channels/{channelId}/invite")
    ApiResponse<Void> addMemberToChannel(
            @AuthenticationPrincipal Jwt principal,
            @PathVariable String serverId,
            @PathVariable String channelId,
            @RequestBody @Valid AddToChannelRequest request) {
        channelService.addMemberToPrivateChannel(serverId, channelId, request);
        return ApiResponse.<Void>builder().message("Member invited to channel successfully").build();
    }
}
