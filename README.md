# O(n)-Logistics: 대규모 물류 관리 시스템
## 📌 프로젝트 소개
O(n)-Logistics는 대량의 상품을 효율적으로 물류 및 배송 프로세스를 체계적으로 관리할 수 있는 시스템입니다.
허브-스포크 구조의 물류 네트워크를 기반으로 각 거점 간의 상품 이동을 최적화하고 실시간으로 배송 상태를 추적할 수 있도록 설계되었습니다. 사용자는 상품의 입·출고, 재고 관리, 배송 경로 설정 등을 체계적으로 관리할 수 있으며 효율적인 운영을 위한 다양한 기능을 제공합니다.
</br>
</br>

## 📌 프로젝트 목적
효율적인 물류 관리 및 배송 프로세스를 구축하여, 상품의 이동과 배송을 체계적으로 운영할 수 있는 시스템을 개발하는 것이 목표입니다.
</br>
</br>

## 📌 프로젝트 기능
### 📄 주문 기능
> * 주문이 생성될 때, 배송 및 배송 경로 기록 데이터가 함께 생성되어 전체적인 흐름을 관리합니다.
> * 주문 생성 시 재고 차감, 취소 시 기존에 차감된 재고 복원됩니다.


### 📝 배송 기록 기능
> * 최종 목적지에 도착하기까지의 모든 경로를 추적할 수 있도록 배송 경로 기록과 연계됩니다.
> * 배송의 전체적인 흐름을 관리하며, 배송 상태가 변경될 때마다 기록됩니다.


### 🚛 허브 간 이동 관리 기능
> * 허브 간 이동 경로를 효율적으로 모델링하여 물류 이동을 최적화합니다.
> * 허브-스포크 구조를 기반으로 하여 물류 거점 간 이동을 효율적으로 처리합니다.


### 🚚 배송 담당자 지정 기능
> * 허브에 도착한 물류를 허브 배송 담당자 또는 업체 배송 담당자에게 자동 할당합니다.
> * 주문과 연결된 배송 정보를 바탕으로 배송 경로 및 담당자 정보가 자동으로 설정됩니다.
</br>
</br>

## 🚨 Trouble Shooting
MSA 내 인증 전파 트러블
OpenFeign 실패 요청 예외 처리 트러블
</br>
</br>

## 📋 ERD Diagram
![image](https://github.com/user-attachments/assets/2999dec3-0a31-426a-9c01-48c4124e123a)
</br>
</br>

## 🌐 Architecture
![image](https://github.com/user-attachments/assets/00032e15-05ab-4894-b7b2-9b77cbff59dd)
</br>
</br>

## 🛠️ 기술 스택
### ⚡️Language & Framework
![springboot](https://img.shields.io/badge/spring&nbsp;boot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)
![springcloud](https://img.shields.io/badge/spring&nbsp;cloud-%236DB33F.svg?style=for-the-badge&logo=springcloud&logoColor=white)

### 💾 Database 
![](https://img.shields.io/badge/postgresql-4479A1?style=for-the-badge&logo=postgresql&logoColor=white)
![](https://img.shields.io/badge/redis-DD0031?style=for-the-badge&logo=redis&logoColor=white)

### 🐳 Containerization
![](https://img.shields.io/badge/docker-339AF0?style=for-the-badge&logo=docker&logoColor=white)

### 🖥️ Monitoring Tools
![](https://img.shields.io/badge/grafana-%23F46800.svg?style=for-the-badge&logo=grafana&logoColor=white)
![](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=Prometheus&logoColor=white)

### 🛠 Tools
![](https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white)
![](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=Postman&logoColor=white)
</br>
</br>

## 👋🏻 팀원 소개
|           [최동인](https://github.com/Bulgogi-Pizza)           |           [백지환](https://github.com/JihwanB)           |                 [전주호](https://github.com/jeonjuho23)                 |         [윤한나](https://github.com/hyper-log)          |         [김소민](https://github.com/ss0ming)          |
| :--------------------------------------------------------------: | :--------------------------------------------------------------: | :--------------------------------------------------------------------------: | :-----------------------------------------------------------: |:-----------------------------------------------------------: |
|      <img src="https://avatars.githubusercontent.com/Bulgogi-Pizza" width="120px;" alt=""/>      |      <img src="https://avatars.githubusercontent.com/JihwanB" width="120px;" alt=""/>      |            <img src="https://avatars.githubusercontent.com/jeonjuho23" width="120px;" alt=""/>            |    <img src="https://avatars.githubusercontent.com/hyper-log" width="120px;" alt=""/>     |    <img src="https://avatars.githubusercontent.com/ss0ming" width="120px;" alt=""/>     |
|                            BE / 인증·인가 / 회원                           |                            BE / 허브 간 이동정보 / 슬랙 / 지도                         |                                  BE / 주문 / AI                                |                         BE / 배송 / 업체 / 상품                          |                          BE / 허브 / 배송 담당자                          |

