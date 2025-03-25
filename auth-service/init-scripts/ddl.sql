CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- auth_role_enum ENUM TYPE 생성
CREATE TYPE auth_role_enum AS ENUM (
    'MASTER',           -- 마스터
    'DELIVERY_MANAGER', -- 배송 담당자
    'HUB_MANAGER',      -- 허브 담당자
    'COMPANY_MANAGER'     -- 업체 담당자
);

-- 인증 테이블
CREATE TABLE p_auth (
                        id UUID PRIMARY KEY DEFAULT (uuid_generate_v4()),
                        user_id UUID NOT NULL,
                        username VARCHAR(50) NOT NULL,
                        password VARCHAR(100) NOT NULL,
                        role auth_role_enum NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        created_by UUID NOT NULL,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        updated_by UUID NOT NULL,
                        is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
                        deleted_at TIMESTAMP NULL,
                        deleted_by UUID NULL
);