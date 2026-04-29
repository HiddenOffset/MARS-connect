#basic program that echos your bytes back at you
.data
buf: .space 512

msg_listen: .asciiz "Listening...\n"
msg_conn:   .asciiz "Client connected\n"
msg_recv:   .asciiz "Received bytes: "

.text
main:
    # listen
    li   $v0, 1005
    li   $a0, 8080
    syscall
    move $s0, $v0

    # print listening
    li   $v0, 4
    la   $a0, msg_listen
    syscall

    # accept (blocks)
    li   $v0, 1006
    move $a0, $s0
    syscall
    move $s1, $v0

    # print connected
    li   $v0, 4
    la   $a0, msg_conn
    syscall

    # recv
    li   $v0, 1004
    move $a0, $s1
    la   $a1, buf
    li   $a2, 512
    syscall
    move $s2, $v0

    # print bytes received
    li   $v0, 4
    la   $a0, msg_recv
    syscall

    li   $v0, 1
    move $a0, $s2
    syscall

    # echo back
    li   $v0, 1003
    move $a0, $s1
    la   $a1, buf
    move $a2, $s2
    syscall

    # close client
    li   $v0, 1002
    move $a0, $s1
    syscall

    # close server
    li   $v0, 1002
    move $a0, $s0
    syscall

    li   $v0, 10
    syscall