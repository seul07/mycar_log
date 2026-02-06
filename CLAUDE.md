# CLAUDE.md

## 자동차 비용 관리 어플 프로젝트 (mycar_log)
- 자동차를 소유하게 되면서 발생하는 비용 전부를 관리하고 분석하는 프로그램을 만들기 위한 목적. 하위에 비용에 포함 되는 목록 들을 정의할 예정.
- 메인 페이지 
    - 자동차 리스트 : 리스트 하위에 자동차 추가 버튼으로 새로운 자동차를 추가 할 수 있다.(로그인 기능 없이, 사용자 개별 기기에 따라 내용을 저장 하는 방식 Firebase Authentication 사용)
    - 자동차 추가 폼 : 자동차 기본 정보를 기입 하는 페이지. 자동차 회사, 자동차 명, 연식, 현재 주행거리, 자동차 종류(전기차 or 일반 으로 정비 목록이 구분 됨) 
    - 특정 자동차 상세페이지를 클릭한 경우 : 현재 달력으로 표기 (해당 달력에는 요일별로 아이콘으로 사용자가 등록한 지출 내역이 간략하게 보여짐), 요일별로 클릭해서 상세 페이지로 넘어감
    - 하위에 홈 , 통계 , 리포트, 설정 바가 있다. 
- 요일 상세 페이지 
  - 요일별 상세 페이지에서 지출 내역을 등록 할수 있다.
  - 첫 페이지에는 아무 내용이 없고 + 즉 추가 버튼을 클릭해서 필요한 카테고리를 추가할 수 있다. 
    - 현재 주행거리 : 바로 이전 까지의 주행 거리와 계산해서 해당 기간 동안의 주행거리를 함께 보여준다.
    - 주유 비용 : 주유시 리터당 금액도 함께 기입 할 수 있다.
    - 정비 : 정비를 클릭 하면 일반 적인 정비 목록 리스트를 보여줘서 사용자가 선택 할 수 있도록 한다. 해당 정비 비용도 함께 기입.
    - 세금 : 지불한 연간 자동차세를 기입 할 수 있다.
    - 보험 : 자동차 보험 또는 운전자 보험을 기입 할 수 있다.
    - 주차 : 주차 비용을 기입 할수 있다.
    - 세차 : 세차 비용을 기입 할수 있다.
    - 기타 : 어플이 제공하지 않는 지출 내용을 사용자가 직접 입력 할 수 있도록 함.
- 통계 페이지
  - 자동차 별로 기간을 설정하여 통계 정보를 확인 할 수 있음.
  - 지출 분류 별로 기간별 치출 내역을 막대 차트로 확인 할 수 있음.
  - 기간 별, 카테고리 별 지출 비율을 도넛 차트로 확인 할 수 있음. 
- 리포트 페이지
  - 사용자가 선택한 날짜 별로 전체 적인 비용을 정리한 페이지 개발 예정.
- 설정 페이지
  - 사용자 정비 목록 페이지로 넘어갈 수 있다. 
- 사용자 정비 목록
  - 사용자가 직접 기본 등록된 정비 목록 내용들에 더해서 지속적인 정비 내용을 추가 및 삭제 할 수 있음.
  - 이미 특정 요일에 등록이 되어 있는 정비 목록은 삭제할 수 없음.
  - 정비 목록 추가 폼은 정비 제목, 상세 내용 을 기입할 수 있음

## 기술 상세
- 필요한 테이블 DDL 명령어를 같은 경로에 table_create.md 파일로 작성해서 저장한다.
- Filebase Authentication(Filebaes Admin SDK) 를 사용하여 자동으로 고유 UID 를 부여한다. 나중에 이메일/소셜 로그인으로 전환 가능성 열어둠. 
- 로컬에서 웹페이지에서 테스트 할 수 있도록 환경을 구성 한다.
- 모든 UI 는 웹페이지, 모바일 등 유동적으로 바뀌는 반응형 웹으로 개발한다.
- 다중 언어로 개발할 예정. 사용자 기기가 설정한 언어별로 자동으로 언어를 변경한다.
- 운영에서 Docker 를 활용해서 빌드 및 실행 할 예정이다. 그리고 CI/CD 를 적용 할 것이다.
- 프로그램을 직접 빌드 또는 실행하지 않는다.
- 사용자가 직접 수정한 내용은 덮어쓰기 하지 않는다.

## 기본 자동차 정비 목록
각각의 부품들이 교체해야 하는 이유를 간락하게 상세 내용에 기입함
- 일반 자동차
  - 활대링크
  - 로워암
  - 타이로드엔드
  - 쇼바
  - 브레이크 패드
  - 브레이크 디스크
  - 드럼 브레이크
  - 브레이크 오일
  - 엔진미미세트
  - 배터리
  - 엔진오일
  - 오일필터
  - 에어필터
  - 에어컨 필터
  - 부동액
- 전기 자동차
  - 일반적으로 전기 자동차가 발생하는 정비 목록을 15개 정도 검색하여 제목, 상세 내용을 해당 하위에 기입한다. 

## Project Details

- **Spring Boot 4.0.2** with **WebMVC** (servlet-based, not reactive/WebFlux).
- **Java 21** toolchain (configured in `build.gradle`; ensure JDK 21+ is installed).
- **Gradle 9.3.0** wrapper — always use `gradlew` / `gradlew.bat`, do not rely on a system Gradle installation.
- Base package: `com.joy.plugins.mycar_log`
- Application entry point: `src/main/java/com/joy/plugins/mycar_log/MycarLogApplication.java`
- Configuration: `src/main/resources/application.properties`

## Architecture Notes

This is an early-stage scaffold (0.0.1-SNAPSHOT). Currently contains only the `@SpringBootApplication` bootstrap class and a context-load smoke test. New controllers, services, and repositories should be added under the `com.joy.plugins.mycar_log` package so that component scanning picks them up automatically.

## 26.02.05
- 리포트 페이지 개발 : 사용자가 선택한 자동차 별로 기간을 정해서 카테고리별로 발생한 비용과 기간별 주행 거리 및 총 주행거리를 리포트로 보여준다.

## 빌드 및 배포
- Docker compose 작성 및 Dockerfile 작성
- 배포 스크립트 작성
- CI/CD 파이프라인 설정 파일 작성