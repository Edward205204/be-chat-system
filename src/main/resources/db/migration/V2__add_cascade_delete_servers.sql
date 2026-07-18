-- V2__add_cascade_delete.sql

-- channels → servers
ALTER TABLE channels
DROP CONSTRAINT fkacxri6fsdtdwloddqhewkt62w,
ADD CONSTRAINT fkacxri6fsdtdwloddqhewkt62w
    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE;

-- invite_links → servers
ALTER TABLE invite_links
DROP CONSTRAINT fk3pu18lykm3pjnvdq1xdccfc28,
ADD CONSTRAINT fk3pu18lykm3pjnvdq1xdccfc28
    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE;

-- messages → channels
ALTER TABLE messages
DROP CONSTRAINT fk3u3ckbhwq9se1cmopk2pq05b2,
ADD CONSTRAINT fk3u3ckbhwq9se1cmopk2pq05b2
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE;

-- roles → servers
ALTER TABLE roles
DROP CONSTRAINT fk3qp1f6wvqvdsl2r7kfrianat,
ADD CONSTRAINT fk3qp1f6wvqvdsl2r7kfrianat
    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE;

-- server_members → servers
ALTER TABLE server_members
DROP CONSTRAINT fkqu0vrc783yq288y2r92gjurw2,
ADD CONSTRAINT fkqu0vrc783yq288y2r92gjurw2
    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE;

-- server_bans → servers
ALTER TABLE server_bans
DROP CONSTRAINT fksbaqqhw6or2x2t9mb5ulxjy27,
ADD CONSTRAINT fksbaqqhw6or2x2t9mb5ulxjy27
    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE;

-- server_invitations → servers
ALTER TABLE server_invitations
DROP CONSTRAINT fk1j8trsiap9027pkq6tql00ew9,
ADD CONSTRAINT fk1j8trsiap9027pkq6tql00ew9
    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE;

-- role_members → roles
ALTER TABLE role_members
DROP CONSTRAINT fklgrk3163r7ti5ctytr1gf48t9,
ADD CONSTRAINT fklgrk3163r7ti5ctytr1gf48t9
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

-- channel_role_permissions → channels
ALTER TABLE channel_role_permissions
DROP CONSTRAINT fk8qtwvwvon178inxgn8nocimbt,
ADD CONSTRAINT fk8qtwvwvon178inxgn8nocimbt
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE;

-- channel_role_permissions → roles
ALTER TABLE channel_role_permissions
DROP CONSTRAINT fko8y91sh1g02763d03f1px80kb,
ADD CONSTRAINT fko8y91sh1g02763d03f1px80kb
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

-- channel_user_permissions → channels
ALTER TABLE channel_user_permissions
DROP CONSTRAINT fk6lrlcbr48iqjyxc22omxkmia9,
ADD CONSTRAINT fk6lrlcbr48iqjyxc22omxkmia9
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE;

-- channel_user_permissions → server_members
ALTER TABLE channel_user_permissions
DROP CONSTRAINT fk5gkg5xmiwb20yhabrjx29ddux,
ADD CONSTRAINT fk5gkg5xmiwb20yhabrjx29ddux
    FOREIGN KEY (server_member_id) REFERENCES server_members(id) ON DELETE CASCADE;

-- role_members → server_members
ALTER TABLE role_members
DROP CONSTRAINT fk9fsbifhuwn9n0dys0qkfj4bbq,
ADD CONSTRAINT fk9fsbifhuwn9n0dys0qkfj4bbq
    FOREIGN KEY (server_member_id) REFERENCES server_members(id) ON DELETE CASCADE;

-- server_role_permission → roles
ALTER TABLE server_role_permission
DROP CONSTRAINT fkp82sjvk98q3s95b9ecu7n8wyb,
ADD CONSTRAINT fkp82sjvk98q3s95b9ecu7n8wyb
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;
