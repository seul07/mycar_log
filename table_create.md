# Database DDL - mycar_log

## Database & Schema Info
- Host: `${DB_HOST}:${DB_PORT}` (see `.env`)
- Database: `${DB_NAME}` (see `.env`)
- Schema: car_log
- Username: `${DB_USERNAME}` (see `.env`)
- Password: `${DB_PASSWORD}` (see `.env`)

---

## 1. Users Table (사용자)

Firebase Authentication UID를 저장하여 사용자를 식별합니다.

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    firebase_uid VARCHAR(128) NOT NULL UNIQUE COMMENT 'Firebase Authentication UID',
    email VARCHAR(255) NULL COMMENT '이메일 (소셜 로그인 전환 시 사용)',
    display_name VARCHAR(100) NULL COMMENT '표시 이름',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_firebase_uid (firebase_uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보';
```

---

## 2. Cars Table (자동차)

사용자별 여러 대의 자동차를 관리합니다.

```sql
CREATE TABLE cars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    manufacturer VARCHAR(100) NOT NULL COMMENT '제조사',
    model_name VARCHAR(100) NOT NULL COMMENT '차량명',
    model_year INT NOT NULL COMMENT '연식',
    initial_mileage BIGINT NOT NULL DEFAULT 0 COMMENT '초기 주행거리 (km)',
    current_mileage BIGINT NOT NULL DEFAULT 0 COMMENT '현재 주행거리 (km)',
    car_type ENUM('GASOLINE', 'ELECTRIC') NOT NULL COMMENT '차량 종류',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='자동차 정보';
```

### 기존 테이블에 컬럼 추가 (마이그레이션)

```sql
ALTER TABLE cars ADD COLUMN current_mileage BIGINT NOT NULL DEFAULT 0 COMMENT '현재 주행거리 (km)' AFTER initial_mileage;

-- 기존 데이터 마이그레이션: current_mileage = initial_mileage
UPDATE cars SET current_mileage = initial_mileage WHERE current_mileage = 0;
```

---

## 3. Maintenance Items Table (정비 항목)

기본 정비 항목 및 사용자 정의 정비 항목을 관리합니다.

```sql
CREATE TABLE maintenance_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '정비 제목',
    description VARCHAR(1000) NULL COMMENT '상세 내용',
    car_type ENUM('GASOLINE', 'ELECTRIC') NOT NULL COMMENT '해당 차량 종류',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '기본 제공 항목 여부',
    car_id BIGINT NULL COMMENT '사용자 정의 항목일 경우 차량 ID',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 여부 (soft delete)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    INDEX idx_car_type (car_type),
    INDEX idx_car_id (car_id),
    INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='정비 항목';
```

---

## 4. Expenses Table (지출 내역)

자동차별 지출 내역을 관리합니다.

```sql
CREATE TABLE expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL COMMENT '자동차 ID',
    expense_date DATE NOT NULL COMMENT '지출 날짜',
    category ENUM('MILEAGE', 'FUEL', 'MAINTENANCE', 'TAX', 'INSURANCE', 'PARKING', 'CAR_WASH', 'OTHER') NOT NULL COMMENT '지출 카테고리',
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '금액',
    current_mileage BIGINT NULL COMMENT '현재 주행거리 (MILEAGE 카테고리)',
    price_per_liter DECIMAL(10, 2) NULL COMMENT '리터당 가격 (FUEL 카테고리)',
    liters DOUBLE NULL COMMENT '주유량 (FUEL 카테고리)',
    maintenance_item_id BIGINT NULL COMMENT '정비 항목 ID (MAINTENANCE 카테고리)',
    insurance_type VARCHAR(50) NULL COMMENT '보험 종류 (INSURANCE 카테고리)',
    description VARCHAR(500) NULL COMMENT '상세 설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    FOREIGN KEY (maintenance_item_id) REFERENCES maintenance_items(id) ON DELETE SET NULL,
    INDEX idx_car_id (car_id),
    INDEX idx_expense_date (expense_date),
    INDEX idx_category (category),
    INDEX idx_car_date (car_id, expense_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='지출 내역';
```

---

## 5. Initial Data - Default Maintenance Items (기본 정비 항목)

### 일반 자동차 (GASOLINE)

```sql
INSERT INTO maintenance_items (title, description, car_type, is_default, is_active) VALUES
('활대링크', '서스펜션의 좌우 움직임을 제어하여 차량 안정성을 유지합니다. 마모 시 핸들링 불안정 발생.', 'GASOLINE', TRUE, TRUE),
('로워암', '서스펜션을 차체에 연결하는 부품. 마모 시 승차감 저하 및 타이어 불균일 마모.', 'GASOLINE', TRUE, TRUE),
('타이로드엔드', '스티어링 시스템의 핵심 부품. 마모 시 핸들 유격 발생 및 조향 불안정.', 'GASOLINE', TRUE, TRUE),
('쇼바', '충격을 흡수하여 승차감을 유지. 마모 시 승차감 저하 및 제동거리 증가.', 'GASOLINE', TRUE, TRUE),
('브레이크 패드', '제동 시 디스크와 마찰하는 부품. 마모 시 제동력 저하 및 소음 발생.', 'GASOLINE', TRUE, TRUE),
('브레이크 디스크', '브레이크 패드와 함께 작동. 마모 시 제동 진동 및 제동력 저하.', 'GASOLINE', TRUE, TRUE),
('드럼 브레이크', '후륜 제동장치. 마모 시 제동력 저하.', 'GASOLINE', TRUE, TRUE),
('브레이크 오일', '유압으로 제동력 전달. 수분 흡수 시 비등점 저하로 베이퍼 록 발생 가능.', 'GASOLINE', TRUE, TRUE),
('엔진마운트', '엔진 진동을 흡수. 마모 시 진동 증가 및 소음 발생.', 'GASOLINE', TRUE, TRUE),
('배터리', '시동 및 전기장치 작동. 수명 다하면 시동 불가.', 'GASOLINE', TRUE, TRUE),
('엔진오일', '엔진 윤활 및 냉각. 교체 불이행 시 엔진 마모 가속.', 'GASOLINE', TRUE, TRUE),
('오일필터', '엔진오일의 불순물 제거. 막히면 엔진 손상.', 'GASOLINE', TRUE, TRUE),
('에어필터', '흡입 공기의 먼지 제거. 막히면 연비 저하 및 출력 감소.', 'GASOLINE', TRUE, TRUE),
('에어컨 필터', '실내 공기 정화. 막히면 냉방 효율 저하 및 악취 발생.', 'GASOLINE', TRUE, TRUE),
('부동액', '엔진 냉각 및 동결 방지. 열화 시 냉각 효율 저하 및 부식 발생.', 'GASOLINE', TRUE, TRUE);
```

### 전기 자동차 (ELECTRIC)

```sql
INSERT INTO maintenance_items (title, description, car_type, is_default, is_active) VALUES
('고전압 배터리 점검', '배터리 상태 및 용량 확인. 성능 저하 시 주행거리 감소.', 'ELECTRIC', TRUE, TRUE),
('배터리 냉각시스템', '배터리 온도 관리. 고장 시 배터리 수명 단축.', 'ELECTRIC', TRUE, TRUE),
('브레이크 패드', '회생제동으로 마모가 적지만 정기 점검 필요.', 'ELECTRIC', TRUE, TRUE),
('브레이크 디스크', '패드와 함께 정기 점검 필요.', 'ELECTRIC', TRUE, TRUE),
('브레이크 오일', '2-3년 주기 교환 권장.', 'ELECTRIC', TRUE, TRUE),
('에어컨 필터', '실내 공기 정화. 6개월-1년 주기 교환.', 'ELECTRIC', TRUE, TRUE),
('타이어 점검', '전기차는 무게와 토크로 마모가 빠름. 정기 점검 필요.', 'ELECTRIC', TRUE, TRUE),
('타이어 로테이션', '균일한 마모를 위해 1-2만km 주기 실시.', 'ELECTRIC', TRUE, TRUE),
('휠 얼라인먼트', '타이어 불균일 마모 방지. 연 1회 점검 권장.', 'ELECTRIC', TRUE, TRUE),
('와이퍼 블레이드', '시야 확보. 6개월-1년 주기 교환.', 'ELECTRIC', TRUE, TRUE),
('12V 보조배터리', '차량 전자장치 작동. 3-5년 주기 교환.', 'ELECTRIC', TRUE, TRUE),
('감속기 오일', '모터 감속기 윤활. 제조사 권장 주기 교환.', 'ELECTRIC', TRUE, TRUE),
('서스펜션 점검', '전기차 무게로 인한 마모 확인.', 'ELECTRIC', TRUE, TRUE),
('충전포트 점검', '충전 단자 오염 및 손상 확인.', 'ELECTRIC', TRUE, TRUE),
('냉각수', '모터 및 인버터 냉각. 제조사 권장 주기 교환.', 'ELECTRIC', TRUE, TRUE);
```

---

## Execution Order

1. Database 및 Schema 생성 (이미 존재한다고 가정)
2. Users 테이블 생성
3. Cars 테이블 생성
4. Maintenance Items 테이블 생성
5. Expenses 테이블 생성
6. 기본 정비 항목 데이터 삽입
