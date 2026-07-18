--
-- PostgreSQL database dump
--

\restrict vWjVK7F5McJTBUI9mQb6uVMLE4GhydPSAnX7I4eSBLN476AcNza0rri0xZ9admq

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: channel_role_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channel_role_permissions (
    id character varying(255) NOT NULL,
    permission character varying(255) NOT NULL,
    channel_id character varying(255) NOT NULL,
    role_id character varying(255) NOT NULL,
    CONSTRAINT channel_role_permissions_permission_check CHECK (((permission)::text = ANY ((ARRAY['NONE'::character varying, 'VIEW_CHANNEL'::character varying, 'MANAGE_CHANNEL'::character varying, 'MANAGE_CHANNEL_PERMISSIONS'::character varying, 'INVITE_MEMBERS'::character varying, 'SEND_MESSAGES'::character varying, 'MANAGE_MESSAGES'::character varying])::text[])))
);


ALTER TABLE public.channel_role_permissions OWNER TO postgres;

--
-- Name: channel_user_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channel_user_permissions (
    id character varying(255) NOT NULL,
    permission character varying(255) NOT NULL,
    channel_id character varying(255) NOT NULL,
    server_member_id character varying(255) NOT NULL,
    CONSTRAINT channel_user_permissions_permission_check CHECK (((permission)::text = ANY ((ARRAY['NONE'::character varying, 'VIEW_CHANNEL'::character varying, 'MANAGE_CHANNEL'::character varying, 'MANAGE_CHANNEL_PERMISSIONS'::character varying, 'INVITE_MEMBERS'::character varying, 'SEND_MESSAGES'::character varying, 'MANAGE_MESSAGES'::character varying])::text[])))
);


ALTER TABLE public.channel_user_permissions OWNER TO postgres;

--
-- Name: channels; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channels (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    is_private boolean NOT NULL,
    name character varying(255) NOT NULL,
    server_id character varying(255) NOT NULL
);


ALTER TABLE public.channels OWNER TO postgres;

--
-- Name: invite_links; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.invite_links (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    is_revoked boolean NOT NULL,
    token character varying(255) NOT NULL,
    use_count integer NOT NULL,
    server_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.invite_links OWNER TO postgres;

--
-- Name: messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.messages (
    id character varying(255) NOT NULL,
    content oid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    channel_id character varying(255) NOT NULL,
    sender_id character varying(255) NOT NULL
);


ALTER TABLE public.messages OWNER TO postgres;

--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.refresh_tokens (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    token character varying(512) NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.refresh_tokens OWNER TO postgres;

--
-- Name: role_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role_members (
    id character varying(255) NOT NULL,
    role_id character varying(255) NOT NULL,
    server_member_id character varying(255) NOT NULL
);


ALTER TABLE public.role_members OWNER TO postgres;

--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id character varying(255) NOT NULL,
    color character varying(7),
    created_at timestamp(6) without time zone NOT NULL,
    is_default boolean NOT NULL,
    name character varying(255) NOT NULL,
    server_id character varying(255) NOT NULL
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: server_bans; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.server_bans (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    reason character varying(255),
    banned_by character varying(255) NOT NULL,
    server_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.server_bans OWNER TO postgres;

--
-- Name: server_invitations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.server_invitations (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    invitee_id character varying(255) NOT NULL,
    inviter_id character varying(255) NOT NULL,
    server_id character varying(255) NOT NULL,
    CONSTRAINT server_invitations_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.server_invitations OWNER TO postgres;

--
-- Name: server_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.server_members (
    id character varying(255) NOT NULL,
    is_muted boolean NOT NULL,
    joined_at timestamp(6) without time zone NOT NULL,
    server_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.server_members OWNER TO postgres;

--
-- Name: server_role_permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.server_role_permission (
    id character varying(255) NOT NULL,
    permission character varying(255) NOT NULL,
    role_id character varying(255) NOT NULL,
    CONSTRAINT server_role_permission_permission_check CHECK (((permission)::text = ANY ((ARRAY['NONE'::character varying, 'MANAGE_CHANNELS'::character varying, 'CREATE_INVITE'::character varying, 'MANAGE_SERVER'::character varying, 'KICK_MEMBER'::character varying, 'BAN_MEMBER'::character varying, 'MUTE_MEMBER'::character varying, 'MANAGE_ROLES'::character varying])::text[])))
);


ALTER TABLE public.server_role_permission OWNER TO postgres;

--
-- Name: servers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.servers (
    id character varying(255) NOT NULL,
    avatar character varying(255),
    banner character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    name character varying(255) NOT NULL,
    owner_id character varying(255) NOT NULL
);


ALTER TABLE public.servers OWNER TO postgres;

--
-- Name: uploaded_files; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.uploaded_files (
    id character varying(255) NOT NULL,
    claimed boolean NOT NULL,
    content_type character varying(255),
    filename character varying(255),
    size bigint NOT NULL,
    uploaded_at timestamp(6) without time zone NOT NULL,
    url character varying(255)
);


ALTER TABLE public.uploaded_files OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id character varying(255) NOT NULL,
    avatar character varying(255),
    banner character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    date_of_birth date,
    display_name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    is_verified boolean NOT NULL,
    password character varying(255) NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: verification_codes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.verification_codes (
    id character varying(255) NOT NULL,
    attempt_count integer NOT NULL,
    code character varying(6) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    last_sent_at timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL,
    CONSTRAINT verification_codes_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'VERIFIED'::character varying, 'REVOKED'::character varying])::text[]))),
    CONSTRAINT verification_codes_type_check CHECK (((type)::text = ANY ((ARRAY['EMAIL_VERIFY'::character varying, 'RESET_PASSWORD'::character varying])::text[])))
);


ALTER TABLE public.verification_codes OWNER TO postgres;

--
-- Name: channel_role_permissions channel_role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_role_permissions
    ADD CONSTRAINT channel_role_permissions_pkey PRIMARY KEY (id);


--
-- Name: channel_user_permissions channel_user_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_user_permissions
    ADD CONSTRAINT channel_user_permissions_pkey PRIMARY KEY (id);


--
-- Name: channels channels_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channels
    ADD CONSTRAINT channels_pkey PRIMARY KEY (id);


--
-- Name: invite_links invite_links_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invite_links
    ADD CONSTRAINT invite_links_pkey PRIMARY KEY (id);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: role_members role_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_members
    ADD CONSTRAINT role_members_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: server_bans server_bans_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_bans
    ADD CONSTRAINT server_bans_pkey PRIMARY KEY (id);


--
-- Name: server_invitations server_invitations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_invitations
    ADD CONSTRAINT server_invitations_pkey PRIMARY KEY (id);


--
-- Name: server_members server_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_members
    ADD CONSTRAINT server_members_pkey PRIMARY KEY (id);


--
-- Name: server_role_permission server_role_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_role_permission
    ADD CONSTRAINT server_role_permission_pkey PRIMARY KEY (id);


--
-- Name: servers servers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servers
    ADD CONSTRAINT servers_pkey PRIMARY KEY (id);


--
-- Name: channels uc_channel_name_server_id; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channels
    ADD CONSTRAINT uc_channel_name_server_id UNIQUE (name, server_id);


--
-- Name: uploaded_files uc_uploadedfile_filename; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.uploaded_files
    ADD CONSTRAINT uc_uploadedfile_filename UNIQUE (filename);


--
-- Name: uploaded_files uc_uploadedfile_url; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.uploaded_files
    ADD CONSTRAINT uc_uploadedfile_url UNIQUE (url);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: server_invitations uk7y1t82tv4os9hhgw4hsv77rn7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_invitations
    ADD CONSTRAINT uk7y1t82tv4os9hhgw4hsv77rn7 UNIQUE (server_id, invitee_id);


--
-- Name: roles uk_roles_server_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT uk_roles_server_name UNIQUE (server_id, name);


--
-- Name: verification_codes uk_verification_user_type; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verification_codes
    ADD CONSTRAINT uk_verification_user_type UNIQUE (user_id, type);


--
-- Name: server_role_permission ukalw58avvw3g17aahewtrem8d; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_role_permission
    ADD CONSTRAINT ukalw58avvw3g17aahewtrem8d UNIQUE (role_id, permission);


--
-- Name: refresh_tokens ukghpmfn23vmxfu3spu3lfg4r2d; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT ukghpmfn23vmxfu3spu3lfg4r2d UNIQUE (token);


--
-- Name: server_members ukm5xw7qbb0cxjm18enyhxl48vh; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_members
    ADD CONSTRAINT ukm5xw7qbb0cxjm18enyhxl48vh UNIQUE (server_id, user_id);


--
-- Name: invite_links ukmqi1xfyo98d63updb93qtp0lc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invite_links
    ADD CONSTRAINT ukmqi1xfyo98d63updb93qtp0lc UNIQUE (token);


--
-- Name: role_members uknh7mjg4muufqlbhkbiphiaegv; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_members
    ADD CONSTRAINT uknh7mjg4muufqlbhkbiphiaegv UNIQUE (role_id, server_member_id);


--
-- Name: server_bans uknsiq2k9g38nqhf61mepp6f08l; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_bans
    ADD CONSTRAINT uknsiq2k9g38nqhf61mepp6f08l UNIQUE (server_id, user_id);


--
-- Name: channel_role_permissions ukp8b4at006ssn6eei1lhq031j3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_role_permissions
    ADD CONSTRAINT ukp8b4at006ssn6eei1lhq031j3 UNIQUE (channel_id, role_id, permission);


--
-- Name: users ukr43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: channel_user_permissions ukvn6wpmm6evdimuhtlvw9x92g; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_user_permissions
    ADD CONSTRAINT ukvn6wpmm6evdimuhtlvw9x92g UNIQUE (channel_id, server_member_id, permission);


--
-- Name: uploaded_files uploaded_files_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.uploaded_files
    ADD CONSTRAINT uploaded_files_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: verification_codes verification_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verification_codes
    ADD CONSTRAINT verification_codes_pkey PRIMARY KEY (id);


--
-- Name: idx_channel_server_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_channel_server_id ON public.channels USING btree (server_id, created_at, id);


--
-- Name: idx_crp_permission; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_crp_permission ON public.channel_role_permissions USING btree (permission);


--
-- Name: idx_crp_role_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_crp_role_id ON public.channel_role_permissions USING btree (role_id);


--
-- Name: idx_invite_server_created; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_invite_server_created ON public.invite_links USING btree (server_id, created_at DESC, id DESC);


--
-- Name: idx_messages_channel_cursor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_messages_channel_cursor ON public.messages USING btree (channel_id, created_at DESC, id DESC);


--
-- Name: idx_refreshtoken_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_refreshtoken_user_id ON public.refresh_tokens USING btree (user_id);


--
-- Name: idx_rm_server_member_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_rm_server_member_id ON public.role_members USING btree (server_member_id);


--
-- Name: idx_roles_server_default; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_roles_server_default ON public.roles USING btree (server_id, is_default);


--
-- Name: idx_server_members_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_server_members_user_id ON public.server_members USING btree (user_id);


--
-- Name: idx_servermember_server_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_servermember_server_id ON public.server_members USING btree (server_id, joined_at, id);


--
-- Name: idx_servers_owner_id_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_servers_owner_id_name ON public.servers USING btree (owner_id, name);


--
-- Name: idx_srp_permission_role; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_srp_permission_role ON public.server_role_permission USING btree (permission, role_id);


--
-- Name: idx_uploadedfile_claimed; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_uploadedfile_claimed ON public.uploaded_files USING btree (claimed, uploaded_at);


--
-- Name: server_invitations fk1j8trsiap9027pkq6tql00ew9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_invitations
    ADD CONSTRAINT fk1j8trsiap9027pkq6tql00ew9 FOREIGN KEY (server_id) REFERENCES public.servers(id);


--
-- Name: refresh_tokens fk1lih5y2npsf8u5o3vhdb9y0os; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: server_members fk264q5e480bvf297331scv3nv1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_members
    ADD CONSTRAINT fk264q5e480bvf297331scv3nv1 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: invite_links fk3pu18lykm3pjnvdq1xdccfc28; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invite_links
    ADD CONSTRAINT fk3pu18lykm3pjnvdq1xdccfc28 FOREIGN KEY (server_id) REFERENCES public.servers(id);


--
-- Name: roles fk3qp1f6wvqvdsl2r7kfrianat; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT fk3qp1f6wvqvdsl2r7kfrianat FOREIGN KEY (server_id) REFERENCES public.servers(id);


--
-- Name: messages fk3u3ckbhwq9se1cmopk2pq05b2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fk3u3ckbhwq9se1cmopk2pq05b2 FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: server_invitations fk4l3liirn4ctc4vm7s30tq40p6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_invitations
    ADD CONSTRAINT fk4l3liirn4ctc4vm7s30tq40p6 FOREIGN KEY (invitee_id) REFERENCES public.users(id);


--
-- Name: messages fk4ui4nnwntodh6wjvck53dbk9m; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fk4ui4nnwntodh6wjvck53dbk9m FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: channel_user_permissions fk5gkg5xmiwb20yhabrjx29ddux; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_user_permissions
    ADD CONSTRAINT fk5gkg5xmiwb20yhabrjx29ddux FOREIGN KEY (server_member_id) REFERENCES public.server_members(id);


--
-- Name: server_invitations fk6kn14mgtviiasaun0nreo45pn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_invitations
    ADD CONSTRAINT fk6kn14mgtviiasaun0nreo45pn FOREIGN KEY (inviter_id) REFERENCES public.users(id);


--
-- Name: channel_user_permissions fk6lrlcbr48iqjyxc22omxkmia9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_user_permissions
    ADD CONSTRAINT fk6lrlcbr48iqjyxc22omxkmia9 FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: channel_role_permissions fk8qtwvwvon178inxgn8nocimbt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_role_permissions
    ADD CONSTRAINT fk8qtwvwvon178inxgn8nocimbt FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: role_members fk9fsbifhuwn9n0dys0qkfj4bbq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_members
    ADD CONSTRAINT fk9fsbifhuwn9n0dys0qkfj4bbq FOREIGN KEY (server_member_id) REFERENCES public.server_members(id);


--
-- Name: verification_codes fka4qo6nts1xd94owirq5evcpda; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verification_codes
    ADD CONSTRAINT fka4qo6nts1xd94owirq5evcpda FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: channels fkacxri6fsdtdwloddqhewkt62w; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channels
    ADD CONSTRAINT fkacxri6fsdtdwloddqhewkt62w FOREIGN KEY (server_id) REFERENCES public.servers(id);


--
-- Name: servers fkcoqnnqwj926ook0at6p01ygq4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servers
    ADD CONSTRAINT fkcoqnnqwj926ook0at6p01ygq4 FOREIGN KEY (owner_id) REFERENCES public.users(id);


--
-- Name: server_bans fkf0bdt92atuapa24bj5j8fmcc8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_bans
    ADD CONSTRAINT fkf0bdt92atuapa24bj5j8fmcc8 FOREIGN KEY (banned_by) REFERENCES public.users(id);


--
-- Name: role_members fklgrk3163r7ti5ctytr1gf48t9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_members
    ADD CONSTRAINT fklgrk3163r7ti5ctytr1gf48t9 FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: invite_links fkm666ckv9b7jybg8ih6sicm7te; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invite_links
    ADD CONSTRAINT fkm666ckv9b7jybg8ih6sicm7te FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: channel_role_permissions fko8y91sh1g02763d03f1px80kb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_role_permissions
    ADD CONSTRAINT fko8y91sh1g02763d03f1px80kb FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: server_role_permission fkp82sjvk98q3s95b9ecu7n8wyb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_role_permission
    ADD CONSTRAINT fkp82sjvk98q3s95b9ecu7n8wyb FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: server_members fkqu0vrc783yq288y2r92gjurw2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_members
    ADD CONSTRAINT fkqu0vrc783yq288y2r92gjurw2 FOREIGN KEY (server_id) REFERENCES public.servers(id);


--
-- Name: server_bans fksbaqqhw6or2x2t9mb5ulxjy27; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_bans
    ADD CONSTRAINT fksbaqqhw6or2x2t9mb5ulxjy27 FOREIGN KEY (server_id) REFERENCES public.servers(id);


--
-- Name: server_bans fkt8jq42cmmbua6w0w3dir3aey0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server_bans
    ADD CONSTRAINT fkt8jq42cmmbua6w0w3dir3aey0 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

\unrestrict vWjVK7F5McJTBUI9mQb6uVMLE4GhydPSAnX7I4eSBLN476AcNza0rri0xZ9admq

