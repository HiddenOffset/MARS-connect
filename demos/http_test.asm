.data
host: .asciiz "wttr.in"

# HTTP/1.0 request to wttr.in for San Diego weather (format %f)
req:  .asciiz "GET /San_Diego_CA?format=%f HTTP/1.0\r\nHost: wttr.in\r\n\r\n"

buf:  .space 2048

msg1: .asciiz "connect: "
msg2: .asciiz "\nsend: "
msg3: .asciiz "\nrecv: "
msg4: .asciiz "\n--- response ---\n"
fail: .asciiz "\nFAILED\n"

.text
main:
    # --- connect ---
    li   $v0, 1001
    la   $a0, host
    li   $a1, 80
    syscall
    move $s0, $v0

    # print connect result
    li   $v0, 4
    la   $a0, msg1
    syscall

    li   $v0, 1
    move $a0, $s0
    syscall

    bltz $s0, failed

    # --- send ---
    li   $v0, 1003
    move $a0, $s0
    la   $a1, req
    li   $a2, 52          # length of request string
    syscall
    move $s1, $v0

    li   $v0, 4
    la   $a0, msg2
    syscall

    li   $v0, 1
    move $a0, $s1
    syscall

    bltz $s1, close_and_done

    # --- recv ---
    li   $v0, 1004
    move $a0, $s0
    la   $a1, buf
    li   $a2, 2047
    syscall
    move $s2, $v0

    li   $v0, 4
    la   $a0, msg3
    syscall

    li   $v0, 1
    move $a0, $s2
    syscall

    blez $s2, close_and_done

    # null-terminate so print_string works
    la   $t0, buf
    addu $t0, $t0, $s2
    sb   $zero, 0($t0)

    # print response
    li   $v0, 4
    la   $a0, msg4
    syscall

    li   $v0, 4
    la   $a0, buf
    syscall

close_and_done:
    # --- close ---
    li   $v0, 1002
    move $a0, $s0
    syscall

    li   $v0, 10
    syscall

failed:
    li   $v0, 4
    la   $a0, fail
    syscall

    li   $v0, 10
    syscall