.data
buf: .space 1024

msg_reset:  .asciiz "reset closed: "
msg_listen: .asciiz "\nListening on port 8080...\n"
msg_conn:   .asciiz "Client connected\n"
msg_fail:   .asciiz "Server setup failed\n"

resp: .asciiz "HTTP/1.0 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>Hello from MIPS!</h1><p>This is your <i>HTTP</i> server!</p></body></html>\n"

.text
main:
    # --- reset once at startup ---
    li   $v0, 1000
    syscall
    move $s7, $v0

    li   $v0, 4
    la   $a0, msg_reset
    syscall

    li   $v0, 1
    move $a0, $s7
    syscall

    # --- listen once ---
    li   $v0, 1005
    li   $a0, 8080
    syscall
    move $s0, $v0
    bltz $s0, failed

    li   $v0, 4
    la   $a0, msg_listen
    syscall

server_loop:
    # --- accept one client ---
    li   $v0, 1006
    move $a0, $s0
    syscall
    move $s1, $v0
    bltz $s1, server_loop

    li   $v0, 4
    la   $a0, msg_conn
    syscall

    # --- recv request (ignored) ---
    li   $v0, 1004
    move $a0, $s1
    la   $a1, buf
    li   $a2, 1024
    syscall
    move $s2, $v0

    # if recv failed, just close client and continue
    bltz $s2, close_client

    # --- send HTML response ---
    move $a0, $s1
    la   $a1, resp
    jal  strlen          # length returned in $v0
    move $a2, $v0
    li   $v0, 1003
    move $a0, $s1
    la   $a1, resp
    syscall

close_client:
    # --- close this client connection ---
    li   $v0, 1002
    move $a0, $s1
    syscall

    # --- go wait for next client ---
    j    server_loop

failed:
    li   $v0, 4
    la   $a0, msg_fail
    syscall

    li   $v0, 10
    syscall
    
strlen:
    move $t0, $a1      # pointer
    li   $v0, 0        # length = 0

strlen_loop:
    lb   $t1, 0($t0)
    beq  $t1, $zero, strlen_done

    addi $t0, $t0, 1
    addi $v0, $v0, 1
    j    strlen_loop

strlen_done:
    jr   $ra