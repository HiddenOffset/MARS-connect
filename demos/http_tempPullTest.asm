.data
host: .asciiz "wttr.in"

# HTTP/1.0 request to wttr.in for San Diego weather (format %f)
req:  .asciiz "GET /San_Diego_CA?format=%f HTTP/1.0\r\nHost: wttr.in\r\n\r\n"

buf:  .space 2048

test_mode: .word 0        # set to 1 to use local test string, 0 to run network
test_str: .asciiz "-10\u00B0F"

msg1: .asciiz "connect: "
msg2: .asciiz "\nsend: "
msg3: .asciiz "\nrecv: "
msg4: .asciiz "\nparsed temp: "
msg5: .asciiz "\n--- response ---\n"
fail: .asciiz "\nFAILED\n"

.text
main:
    # test-mode toggle: if nonzero, copy `test_str` into `buf` and jump to parse
    la   $t6, test_mode
    lw   $t6, 0($t6)
    beq  $t6, $zero, do_connect

    # copy test_str -> buf
    la   $t0, buf
    la   $t1, test_str
copy_loop:
    lb   $t2, 0($t1)
    sb   $t2, 0($t0)
    beq  $t2, $zero, copied
    addiu $t0, $t0, 1
    addiu $t1, $t1, 1
    j    copy_loop

copied:
    # compute length into $s2
    la   $t0, buf
    li   $s2, 0
len_loop:
    lb   $t2, 0($t0)
    beq  $t2, $zero, start_parse
    addiu $s2, $s2, 1
    addiu $t0, $t0, 1
    j    len_loop

start_parse:
    la   $t0, buf
    j    parse_start

do_connect:
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
    li   $a2, 55          # length of request string
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

    # parse the first signed integer from the HTTP body and store it in $s4
    # Supports an optional leading '+' or '-' so negative temps work
    la   $t0, buf

find_body:
    lb   $t1, 0($t0)
    beq  $t1, $zero, failed
    li   $t2, 13
    bne  $t1, $t2, next_body_char
    lb   $t3, 1($t0)
    li   $t2, 10
    bne  $t3, $t2, next_body_char
    lb   $t3, 2($t0)
    li   $t2, 13
    bne  $t3, $t2, next_body_char
    lb   $t3, 3($t0)
    li   $t2, 10
    bne  $t3, $t2, next_body_char
    addiu $t0, $t0, 4
    j    parse_start

next_body_char:
    addiu $t0, $t0, 1
    j    find_body

parse_start:
    li   $s4, 0
    li   $t4, 1

skip_leading:
    lb   $t1, 0($t0)
    beq  $t1, $zero, failed
    li   $t2, '+'
    beq  $t1, $t2, parse_positive
    li   $t2, '-'
    beq  $t1, $t2, parse_negative
    li   $t2, '0'
    blt  $t1, $t2, advance_skip
    li   $t2, '9'
    bgt  $t1, $t2, advance_skip
    j    parse_digits

parse_negative:
    li   $t4, -1
    addiu $t0, $t0, 1
    j    parse_digits

parse_positive:
    addiu $t0, $t0, 1
    j    parse_digits

advance_skip:
    addiu $t0, $t0, 1
    j    skip_leading

parse_digits:
    li   $s4, 0

digit_loop:
    lb   $t1, 0($t0)
    li   $t2, '0'
    blt  $t1, $t2, finish_parse
    li   $t2, '9'
    bgt  $t1, $t2, finish_parse
    addiu $t1, $t1, -48
    mul  $s4, $s4, 10
    addu $s4, $s4, $t1
    addiu $t0, $t0, 1
    j    digit_loop

finish_parse:
    li   $t5, 1
    beq  $t4, $t5, parsed_ok
    subu $s4, $zero, $s4

parsed_ok:
    li   $v0, 4
    la   $a0, msg4
    syscall

    li   $v0, 1
    move $a0, $s4
    syscall

    # null-terminate so print_string works
    la   $t0, buf
    addu $t0, $t0, $s2
    sb   $zero, 0($t0)

    # print response
    li   $v0, 4
    la   $a0, msg5
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
