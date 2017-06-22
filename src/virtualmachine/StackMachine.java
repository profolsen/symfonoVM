package virtualmachine;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by po917265 on 5/22/17.
 */
public class StackMachine {

    //private LinkedList<Integer> stack;

    private Memory memory;

    private int sL; //how low the stack counter can go.

    private int stack;

    private int pc;

    private int index = 0;

    public StackMachine(int memoryCapacity) {
        //stack = new LinkedList<Integer>();
        memory = new Memory(memoryCapacity);
        pc = 0;
        stack = memoryCapacity;
    }

    public int pc() { return pc; }

    public void dupn() {
        int number = pop();
        int[] array = new int[number];
        for(int i = number - 1; i >= 0; i--) {
            array[i] = pop();
        }
        for(int i = 0; i < number; i++) {
            push(array[i]);
        }
        for(int i = 0; i < number; i++) {
            push(array[i]);
        }
    }

    public void push(int v) {
        if(stack == sL) {
            System.out.println("Overfull stack @" + pc);
            System.exit(0);
        }
        memory.set(--stack, v);
    }

    private int pop() {
        if(stack == memory.capacity()) {
            System.out.println("Over empty stack @" + pc);
            System.exit(0);
        }
        int answer = memory.get(stack);
        stack++;
        return answer;
    }

    public void dup() {
        push(1);
        dupn();
    }

    public void dup2() {
        push(2);
        dupn();
    }

    public void dup3() {
        push(3);
        dupn();
    }

    public void add() {
        push(pop() + pop());
    }

    public void mul() {
        push(pop() * pop());
    }

    public void sub() {
        push(pop() - pop());
    }

    public void div() {
        push(pop() / pop());
    }

    private void cmp() { push(Integer.compare(pop(), pop())); }

    public void ld() {
        int index = adjust(memory.get(pc+1));
        push(memory.get(index));
        pc++;  //must skip the next command...
    }

    private void ldi() {
        push(memory.get(pc+1));
        pc++;
    }

    private void down() {
        int val = memory.get(pc+1);
        int x = pop();
        int[] temp = new int[val];
        for(int i = val - 1; i >= 0; i--) {
            temp[i] = pop();
        }
        push(x);
        for(int i = 0; i < val; i++) {
            push(temp[i]);
        }
        pc++;
    }

    public void zero() {
        push(0);
    }

    public void one() {
        push(1);
    }

    public void jmp() {
        pc = pop();
    }

    public void beq() {
        int loc = pop();
        int a = pop();
        int b = pop();
//      System.out.println("beq=" + loc + ", a=" + a + ", b=" + b);
        if(a == b) {
//          System.out.println("EQUAL...");
            pc = loc;
        } else pc++;
    }

    public void sys() {
        int code = memory.get(++pc);
        switch(code) {
            case 0x1 : //printing an integer in decimal.
                //System.out.println("Printing a number to screen...");
                System.out.print(pop());
            break;
            case 0x2 : //printing a character to the screen.
                System.out.print("" + (char)pop());
            break;
            case 0x3 : //printing a newline character to the screen.
                System.out.println();
            break;
            case 0x4:
                try {
                    int x = System.in.read();
                    push(x);
                } catch (IOException e) {
                    System.out.println("IO error");
                    System.exit(0);
                }
            break;
            case 0x0 : //print out debug info... non standard!
                System.out.println("Current State of Stack Machine: ");
                System.out.println("Memory: " + Arrays.toString(memory.from(0)));
                System.out.println("PC: " + pc);
                System.out.println("stack: " + Arrays.toString(memory.from(stack)));
                break;
        }
    }

    public void st() {
        memory.set(adjust(memory.get(pc+1)), pop());
        pc++; //pc must be incremented twice in this case.
    }

    private void exec(int command) {
        /*System.out.println("Current State of Stack Machine: ");
        System.out.println("Memory: " + Arrays.toString(memory));
        System.out.println("PC: " + pc);
        System.out.println("stack: " + Arrays.toString(Arrays.copyOfRange(memory, stack, memory.length)));*/
        switch(command) {
            case 0x0:
                jmp();
                return;
            case 0x1:
                beq();
                return;
            case 0x2:
                ld();
                pc++;
                return;
            case 0x3:
                sys();
                pc++;
                return;
            case 0x4:
                iarith();
                pc++;
                return;
            case 0x5:
                farith();
                pc++;
                return;
            case 0x6:
                //this instruction is undefined.
                pc++;
                return;
            case 0x7:
                //this instruction is undefined.
                pc++;
                return;
            case 0x8:
                zero();
                pc++;
                return;
            case 0x9:
                one();
                pc++;
                return;
            case 0xA:
                dup();
                pc++;
                return;
            case 0xB:
                down();
                pc++;
                //swap(); retired.  too simplistic.  We need something more powerful...
                //retired dup2() because it isn't really useful.
                //dup2();
                return;
            case 0xC:
                //retired dupn() becuase it isn't really useful.
                //dupn();
                //this instruction is undefined.
                pc++;
                return;
            case 0xD:
                ldi();
                pc++;
                return;
            case 0xE:
                st();
                pc++;
                return;
            default:
               System.out.println("Illegal Opcode: " + command);
               System.exit(0);
        }
    }

    private void farith() {
        int code = memory.get(++pc);
        //do nothing since at the moment no floating point operations are defined.
        //we just need to get the code, because the specification requires a code argument.
    }

    private void iarith() {
        int code = memory.get(++pc);
        switch(code) {
            case 0x1:
                push(pop() + pop());
                break;
            case 0x2:
                push(pop() - pop());
                break;
            case 0x3:
                push(pop() * pop());
                break;
            case 0x4:
                push(pop() / pop());
                break;
            case 0x5:
                push(Integer.compare(pop(), pop()));
                break;
        }
    }

    public void run() {
        int command = memory.get(pc);
        int count = 0;
        while (command != 0xF) { //while command is not the halt command...
            exec(command);
            command = memory.get(pc);
            //System.out.println("\tcmd:" + command + "@" + pc);
            count++;
        }
    }

    public void load(int i) {
        memory.set(index++, i);
        sL = index;
    }

    private int adjust(int index) {
        if(index > 0) return index;
        return sL + index;
    }
}
