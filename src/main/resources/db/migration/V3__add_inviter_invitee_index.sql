-- V3__add_inviter_invitee_index.sql

ALTER TABLE server_invitations
DROP CONSTRAINT uk7y1t82tv4os9hhgw4hsv77rn7;

DROP INDEX IF EXISTS idx_invitee_id_inviter_id;

CREATE UNIQUE INDEX uq_invitation_pending
    ON server_invitations (server_id, inviter_id, invitee_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_invitation_invitee_server_status
    ON server_invitations (invitee_id, server_id, status);

CREATE INDEX idx_invitation_invitee_server_inviter
    ON server_invitations (invitee_id, server_id, inviter_id);
