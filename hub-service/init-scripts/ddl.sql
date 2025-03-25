
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- hub_type ENUM TYPE 생성
CREATE TYPE hub_type_enum AS ENUM (
    'HUB',
    'SPOKE'
    );

create table p_hub
(
    id         uuid         default uuid_generate_v4() not null
        primary key,
    created_at timestamp(6) default CURRENT_TIMESTAMP not null,
    deleted_at timestamp(6),
    is_deleted boolean,
    updated_at timestamp(6) default CURRENT_TIMESTAMP not null,
    address    varchar(255)                           not null,
    latitude   numeric(11, 9)                         not null,
    longitude  numeric(12, 8)                         not null,
    name       varchar(255)                           not null,
    type       hub_type_enum NOT NULL,
    created_by uuid,
    updated_by uuid,
    deleted_by uuid
);

create table public.p_center_spoke_hub_link
(
    id         uuid         not null
        primary key,
    created_at timestamp(6) default CURRENT_TIMESTAMP not null,
    created_by uuid,
    deleted_at timestamp(6),
    deleted_by uuid,
    is_deleted boolean,
    updated_at timestamp(6) default CURRENT_TIMESTAMP not null,
    updated_by uuid,
    center_id  uuid
        constraint fkem9jrhpb7u58untr0cui2unxs
            references public.p_hub,
    spoke_id   uuid
        constraint fknihwa1nt73xvovlbbosnle5aq
            references public.p_hub
);

ALTER TABLE p_hub
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

ALTER TABLE p_center_spoke_hub_link
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

alter table public.p_center_spoke_hub_link
    owner to postgres;

