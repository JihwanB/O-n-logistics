-- 기존 데이터 삭제
-- p_center_spoke_hub_link에서 center_id를 참조하는 데이터 삭제
DELETE
FROM p_center_spoke_hub_link
WHERE center_id IN (SELECT id FROM p_hub);

-- p_center_spoke_hub_link에서 spoke_id를 참조하는 데이터 삭제
DELETE
FROM p_center_spoke_hub_link
WHERE spoke_id IN (SELECT id FROM p_hub);

-- 그 후에 p_hub에서 삭제
DELETE
FROM p_hub;


-- 서울특별시 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('60e4dc56-de97-47b1-ac12-4396de5a37e3', '서울특별시 센터', 'SPOKE', '서울특별시 송파구 송파대로 55', 37.506476, 127.105021, false) ON CONFLICT DO NOTHING;

-- 경기 북부 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('9044c2f6-025d-439f-af02-105f39396cbb', '경기 북부 센터', 'SPOKE', '경기도 고양시 덕양구 권율대로 570', 37.655312, 126.837468, false) ON CONFLICT DO NOTHING;

-- 경기 남부 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('735fd294-061c-45dd-b846-fe7ccaee7708', '경기 남부 센터', 'HUB', '경기도 이천시 덕평로 257-21', 37.270838, 127.482994, false) ON CONFLICT DO NOTHING;

-- 부산광역시 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('0ee020ef-d56f-4bc0-abd8-c6a80c8c94f9', '부산광역시 센터', 'SPOKE', '부산 동구 중앙대로 206', 35.103800, 129.040795, false) ON CONFLICT DO NOTHING;

-- 대구광역시 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('533cae8c-fd3f-430b-a5eb-820bcbd58322', '대구광역시 센터', 'HUB', '대구 북구 태평로 161', 35.867060, 128.609730, false) ON CONFLICT DO NOTHING;

-- 인천광역시 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('fb3a6025-5049-44fb-bbd3-af26b5a761cb', '인천광역시 센터', 'SPOKE', '인천 남동구 정각로 29', 37.456590, 126.705051, false) ON CONFLICT DO NOTHING;

-- 광주광역시 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('a0802c17-a56e-479a-a79f-be0ff75075ef', '광주광역시 센터', 'SPOKE', '광주 서구 내방로 111', 35.159545, 126.851430, false) ON CONFLICT DO NOTHING;

-- 대전광역시 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('e6c043a7-bde6-45a6-a231-59fbf43cc81f', '대전광역시 센터', 'HUB', '대전 서구 둔산로 100', 36.350411, 127.384548, false) ON CONFLICT DO NOTHING;

-- 울산광역시 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('3888dc46-279c-4524-9d21-1526872132f5', '울산광역시 센터', 'SPOKE', '울산 남구 중앙로 201', 35.540017, 129.311136, false) ON CONFLICT DO NOTHING;

-- 세종특별자치시 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('3887ef21-281b-4da9-a9bf-b73568580e0e', '세종특별자치시 센터', 'SPOKE', '세종특별자치시 한누리대로 2130', 36.480286, 127.289372, false) ON CONFLICT DO NOTHING;

-- 강원특별자치도 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('34f7eff6-8b95-43a9-af07-9c426d7cdbff', '강원특별자치도 센터', 'SPOKE', '강원특별자치도 춘천시 중앙로 1', 37.880220, 127.727746, false) ON CONFLICT DO NOTHING;

-- 충청북도 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('5f60ff43-856d-4775-b5f8-96a5624f7368', '충청북도 센터', 'SPOKE', '충북 청주시 상당구 상당로 82', 36.635698, 127.492606, false) ON CONFLICT DO NOTHING;

-- 충청남도 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('58f8c0aa-0e06-4b2f-ab65-bc57fb839736', '충청남도 센터', 'SPOKE', '충남 홍성군 홍북읍 충남대로 21', 36.635342, 126.679073, false) ON CONFLICT DO NOTHING;

-- 전북특별자치도 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('659c9eb6-c1ff-44fd-9a8a-840e5480436b', '전북특별자치도 센터', 'SPOKE', '전북특별자치도 전주시 완산구 효자로 225', 35.822823,
        127.150739, false) ON CONFLICT DO NOTHING;

-- 전라남도 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('a6e6539d-4993-4c34-8849-a7ed9e519b27', '전라남도 센터', 'SPOKE', '전남 무안군 삼향읍 오룡길 1', 34.999219, 126.719442, false) ON CONFLICT DO NOTHING;

-- 경상북도 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('908e0d37-8d6c-41b8-a413-c0fd79452498', '경상북도 센터', 'SPOKE', '경북 안동시 풍천면 도청대로 455', 36.565768, 128.739294, false) ON CONFLICT DO NOTHING;

-- 경상남도 센터
INSERT INTO p_hub (id, name, type, address, latitude, longitude, is_deleted)
VALUES ('854bef8c-a9f9-41e6-ad33-89d86e7a765b', '경상남도 센터', 'SPOKE', '경남 창원시 의창구 중앙대로 300', 35.228541, 128.681829, false) ON CONFLICT DO NOTHING;


-- 경기남부 - 경기북부 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '경기 남부 센터'),
        (SELECT id FROM p_hub WHERE name = '경기 북부 센터'));

-- 경기남부 - 서울 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '경기 남부 센터'),
        (SELECT id FROM p_hub WHERE name = '서울특별시 센터'));

-- 경기남부 - 인천 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '경기 남부 센터'),
        (SELECT id FROM p_hub WHERE name = '인천광역시 센터'));

-- 경기남부 - 강원도 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '경기 남부 센터'),
        (SELECT id FROM p_hub WHERE name = '강원특별자치도 센터'));

-- 대전 - 충청남도 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대전광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '충청남도 센터'));

-- 대전 - 충청북도 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대전광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '충청북도 센터'));

-- 대전 - 세종 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대전광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '세종특별자치시 센터'));

-- 대전 - 전라북도 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대전광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '전라북도 센터'));

-- 대전 - 광주 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대전광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '광주광역시 센터'));

-- 대전 - 전라남도 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대전광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '전라남도 센터'));

-- 대구 (5) 연결
-- 대구 - 경상북도 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대구광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '경상북도 센터'));

-- 대구 - 경상남도 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대구광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '경상남도 센터'));

-- 대구 - 부산 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대구광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '부산광역시 센터'));

-- 대구 - 울산 연결
INSERT INTO p_center_spoke_hub_link (center_id, spoke_id)
VALUES ((SELECT id FROM p_hub WHERE name = '대구광역시 센터'),
        (SELECT id FROM p_hub WHERE name = '울산광역시 센터'));