# Trouble Shooting (트러블슈팅 기록)

모아(Moa) 앱 개발 과정에서 발생하는 오류, 발생 원인, 그리고 해결 과정을 기록하는 문서입니다.
문제가 발생할 때마다 아래의 양식을 복사하여 새로운 기록을 최신순으로 추가합니다.

---

## 📝 기록 양식 (Template)

### [오류 요약 또는 제목]
*   **발생 날짜**: YYYY-MM-DD
*   **오류 상황/메시지**: (발생한 오류의 내용이나 증상, 에러 로그 등 기록)
*   **발생 원인**: (문제가 발생한 근본적인 이유 추적)
*   **해결 방법**: (시도한 방법 및 최종적으로 문제를 해결한 구체적인 코드나 방법 기록)
*   **참고 자료**: (해결에 도움이 된 공식 문서, 스택오버플로우 링크 등)

---

## 🛠️ 해결 기록
*(아래부터 새로운 트러블슈팅 내역을 추가합니다.)*

### [minSdkVersion 충돌 오류]
*   **발생 날짜**: 2026-05-15
*   **오류 상황/메시지**: `Manifest merger failed : uses-sdk:minSdkVersion 22 cannot be smaller than version 23 declared in library...`
*   **발생 원인**: 프로젝트의 최소 지원 SDK 버전(`minSdk`)이 22로 설정되어 있으나, 프로젝트에서 사용하는 특정 외부 라이브러리(`androidx.navigationevent`)가 동작하기 위해 최소 SDK 23 이상을 요구하여 충돌이 발생함.
*   **해결 방법**: `app/build.gradle.kts` 파일에서 `minSdk = 22` 부분을 최신 트렌드와 라이브러리 요구사항에 맞추어 `minSdk = 24`로 상향 조정한 후 Sync 진행.
*   **참고 자료**: 안드로이드 공식 문서 (버전 호환성 문제)

### [SVG Vector 변환 오류 (fillColor url 호환성)]
*   **발생 날짜**: 2026-05-15
*   **오류 상황/메시지**: `error: 'url(#pattern0_76_277)' is incompatible with attribute fillColor (attr) color.`
*   **발생 원인**: Figma 등 디자인 툴에서 SVG를 내보낼 때, 안드로이드 VectorDrawable이 지원하지 않는 형태(이미지 패턴 채우기, 복잡한 그림자 필터 등)가 포함되어 있어 XML로 완벽하게 변환되지 못함.
*   **해결 방법**: 해당 이미지는 복잡한 그래픽이 포함되어 있어 SVG(Vector)보다는 PNG나 WebP로 사용하는 것이 적합함. 단, PNG 사용 시 흐릿해지는 현상을 막기 위해 디자인 툴에서 이미지를 **3x 또는 4x 등 고해상도로 크게(Export)** 추출하여 `drawable`에 넣고 사용함.
*   **참고 자료**: 안드로이드 Vector Asset Studio 공식 문서
