#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <errno.h>
#include <fcntl.h>

#define PORT 8080
#define HEADER_LENGTH 7

// 연결 시도 함수 (비동기 소켓을 생성하기 위해 fcntl 사용)
int connect_to_server(struct sockaddr_in *serv_addr) {
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        printf("\n Socket creation error \n");
        return -1;
    }

    // 소켓을 non-blocking 모드로 설정
    int flags = fcntl(sock, F_GETFL, 0);
    fcntl(sock, F_SETFL, flags | O_NONBLOCK);

    if (connect(sock, (struct sockaddr *)serv_addr, sizeof(*serv_addr)) < 0) {
        if (errno != EINPROGRESS) {
            printf("\nConnection Failed \n");
            close(sock);
            return -1;
        }
    }

    printf("Connected to server (non-blocking)\n");
    return sock;
}

// 비동기 데이터 전송 함수
int send_data(int sock, const char *data, int data_length) {
    int sent = 0;
    while (sent < data_length) {
        int bytes_sent = send(sock, data + sent, data_length - sent, 0);
        if (bytes_sent == -1) {
            if (errno == EWOULDBLOCK || errno == EAGAIN) {
                // 소켓이 일시적으로 보내지 못하는 경우, 다시 시도
                continue;
            } else {
                // 다른 에러 발생
                return -1;
            }
        }
        sent += bytes_sent;
    }
    return sent;
}

int main() {
    int sock = 0;
    struct sockaddr_in serv_addr;
    char *data = "0111HDR1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111190111HDR2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222290111HDR3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333390111HDR444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444449";  // 다수의 데이터
    pid_t pid;

    // 서버 주소 설정
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(PORT);

    if (inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr) <= 0) {
        printf("\nInvalid address/ Address not supported \n");
        return -1;
    }

    while (1) {
        // 소켓 연결 시도
        sock = connect_to_server(&serv_addr);
        if (sock == -1) {
            printf("Retrying connection in 5 seconds...\n");
            sleep(5);  // 5초 대기 후 재연결 시도
            continue;
        }

        // fork()로 자식 프로세스 생성
        pid = fork();

        if (pid < 0) {
            printf("Fork failed\n");
            return -1;
        }
        else if (pid == 0) {
            // 자식 프로세스: 데이터를 계속 보냄
            int total_data_length = strlen(data);
            int header_size = 4 + 3;  // 4바이트 길이 + 3바이트 플래그 = 7바이트
            int current_position = 0;

            // postion 지정하고 거기서 계속 위치 더하면서 해야됨
            while (current_position < total_data_length) {
                if (strncmp("HDR", data + current_position + 4, 3) != 0) {
                    continue;
                }
                char len_str[4];
                memcpy(len_str, data + current_position, 4);

                int message_length = atoi(len_str);

                // 각 메시지의 헤더 크기를 제외한 실제 메시지 부분만 전송
                const char *actual_data = data + current_position + header_size;

                // 메시지 본문 전송
                if (send_data(sock, actual_data, message_length) == -1) {
                    printf("Failed to send message. Reconnecting...\n");
                    break;
                }
                printf("Child process: Actual message sent: %.*s\n", message_length, actual_data);

                // 현재 포지션을 다음 메시지로 이동 (헤더 + 메시지 길이만큼 이동)
                current_position += header_size + message_length;

//                sleep(0.5);  // 2초 간격으로 메시지 전송
            }

            close(sock);  // 자식 프로세스에서 소켓 닫기
            exit(0);      // 자식 프로세스 종료
        }
        else {
            // 부모 프로세스: 자식 프로세스가 끝날 때까지 대기
            wait(NULL);
            printf("Parent process: Child process finished. Reconnecting...\n");
        }
    }

    return 0;
}

