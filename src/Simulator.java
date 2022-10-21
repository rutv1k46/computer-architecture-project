import java.util.Arrays;
import java.io.*;
import javax.swing.*;

/**
 * The main class that simulates the operations of the computer
 */
public class Simulator {
    /** General purpose register 1. */
    int[] R0;

    /** General purpose register 2. */
    int[] R1;

    /** General purpose register 3. */
    int[] R2;

    /** General purpose register 4. */
    int[] R3;

    /** Program counter. */
    int[] PC;
    
    /** Conditional counter. */
    int[] CC;
    
    /** Instruction register. */
    int[] IR;

    /** Memory address register. */
    int[] MAR;

    /** Memory buffer register. */
    int[] MBR;

    /** Machine fault register. */
    int[] MFR;

    /** Index register 1. */
    int[] X1;

    /** Index register 2. */
    int[] X2;

    /** Index register 3. */
    int[] X3;

    /** Main memory. */
    Memory M;

    /** Boolean which indicates whether the machine is currently halted. */
    boolean halted = true;

    int[] MAR_INIT = new int[100];
    int lines = 0;

    /** An interface that is notified when the state of this simulated machine changes. */
    Interface I;

    /** Constant array of the valid opcodes as decimal integers. */
    int[] OPCODES           = {   1,    2,    3,   33,   34,   8,    9,   10,   11,   12,   13,   14,   15};
    String[] OPCODES_base8  = {"01", "02", "03", "41", "42", "10", "11", "12", "13", "14", "15", "16", "17"};

    int click =0;

    /** Creates a simulator instance with memory of size size
     * 
     * @param   size    the size of the memory for this simulated computer in 16-bit words
     */
    public Simulator(int size) {
        this.R0 = new int[16];
        this.R1 = new int[16];
        this.R2 = new int[16];
        this.R3 = new int[16];
        this.PC = new int[12];
        this.CC = new int[4];
        this.IR = new int[16];
        this.MAR = new int[12];
        this.MBR = new int[16];
        this.MFR = new int[4];
        this.X1 = new int[16];
        this.X2 = new int[16];
        this.X3 = new int[16];
        this.M = new Memory(size);
    }

    /**
     * Performs a single step of machine execution: executing the instruction in the IR.
     */
    public void step() {
        // Copy address from PC to MAR
        updateRegister("MAR", this.PC);
        
        // Increment PC
        incrementPC();
        
        // Load the MBR with the data from memory at the address specified by the contents of MAR
        load();
        
        // Copy MBR to IR
        registerCopy(this.MBR, this.IR);
        
        // Execute the instruction now in the IR
        executeInstruction();

        // Notify the interface that changes may have occured
        this.I.updateDisplay();

        // Print to console the nonzero contents at this moment
        System.out.println(this.M);
    }

    /**
     * Executes the instruction specified by the contents of the IR.
     */
    public void executeInstruction() {
        // Decode the instruction in the IR
        int[] opcode_array = Arrays.copyOfRange(this.IR, 0, 6);
        int[] R = Arrays.copyOfRange(this.IR, 6, 8);
        int[] IX = Arrays.copyOfRange(this.IR, 8, 10);
        int[] I = Arrays.copyOfRange(this.IR, 10, 11);
        int[] address = Arrays.copyOfRange(this.IR, 11, 16);
        
        // Get the opcode as an integer
        int opcode = Utilities.bin2dec(opcode_array);
        // opcode = 1;
        // Switch on the opcode
        switch (opcode) {
            case 1:               
                // LDR r, x, address[,I]
                System.out.println("opcode " + opcode + ",(LDR) is being executed");
                executeLDR(R, IX, I, address);
                break;
            case 2:
                // STR r, x, address[,I] 
                System.out.println("opcode " + opcode + ",(STR) is being executed");
                executeSTR(R, IX, I, address);
                break;
            case 3:
                // LDA r, x, address[,I]
                System.out.println("opcode " + opcode + ",(LDA) is being executed");
                executeLDA(R,IX,I,address);
                break;
            case 4:
                System.out.println("opcode " + opcode + ",(AMR) is being executed");
                executeAMR(R, IX, I, address);
                break;
            case 5: 
                System.out.println("opcode " + opcode + ",(SMR) is being executed");
                executeSMR(R, IX, I, address);
                break;
            case 6:
                System.out.println("opcode " + opcode + ",(AIR) is being executed");
                executeAIR(R, address);
                break;
            case 7:
                System.out.println("opcode " + opcode + ",(SIR) is being executed");
                executeSIR(R, address);
                break;
            case 33:
                // LDX x, address[,I]
                System.out.println("opcode " + opcode + ",(LDX) is being executed");
                executeLDX(R, IX, I, address);
                break;
            case 34:
                // STX x, address[,I]
                System.out.println("opcode " + opcode + ",(STX) is being executed");
                executeSTX(R, IX, I, address);
                break;
            case 8:
                // JZ r, x, address[,I]
                /* Jump If Zero:
                * If c(r) = 0, then PC  EA
                * Else PC <- PC+1 
                */
                System.out.println("opcode "+opcode+" ,(JZ) is being executed");
                // this.halted = true;
                executeJZ(R, IX, I, address);
                break;
            case 9:
                // JNE r, x, address[,I] 
                System.out.println("opcode "+opcode+" ,(JNE) is being executed");
                // this.halted = true;
                executeJNE(R, IX, I, address);
                break;
            case 10:
                // JCC cc, x, address[,I] 
                System.out.println("opcode "+opcode+" ,(JCC) is being executed");
                // this.halted = true;
                executeJCC(CC, IX, I, address);
                break;
            case 11:
                // JMA x, address[,I]
                System.out.println("opcode "+opcode+" ,(JMA) is being executed");
                // this.halted = true;
                executeJMA(IX, I, address);
                break;
            case 12:
                // JSR x, address[,I] 
                System.out.println("opcode "+opcode+" was given but is not yet implemented");
                this.halted = true;
                break;
            case 13:
                // RFS Immed 
                System.out.println("opcode "+opcode+" ,(RFS) is being executed");
                // this.halted = true;
                executeRFS(address);
                break;
            case 14:
                // SOB r, x, address[,I] 
                System.out.println("opcode "+opcode+" ,(SOB) is being executed");
                // this.halted = true;
                executeSOB(R, IX, I, address);
                break;
            case 15:
                // JGE r,x, address[,I]
                System.out.println("opcode "+opcode+" ,(JGE) is being executed");
                // this.halted = true;
                executeJGE(R, IX, I, address);
                break;
            case 18:
                System.out.println("opcode "+opcode+" ,(TRR) is being executed");
                executeTRR(R, IX);
                break;
            // case 25:
            //     System.out.println("opcode "+opcode+" ,(NOT) is being executed");
            //     executeNOT(R);
            //     break;
            default:
                // Invalid opcode (this opcode is not specified)
                System.out.println("invalid opcode recieved: "+opcode+" in decimal");   
                this.halted = true;         
        }
    }

    private void executeLDR(int[] R, int[] IX, int[] I, int[] address) {

		// calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		int indexingRegister = Utilities.bin2dec(IX);
		switch (indexingRegister) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}

		// indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		// this.I.updateDisplay();
		load(); // copies contents in address of MAR to MBR
		this.I.updateDisplay();
		int targetRegister = Utilities.bin2dec(R);

		switch (targetRegister) {
		case 0:
			registerCopy(this.MBR, this.R0);
			break;
		case 1:
			registerCopy(this.MBR, this.R1);
			break;
		case 2:
			registerCopy(this.MBR, this.R2);
			break;
		case 3:
			registerCopy(this.MBR, this.R3);
			break;
		default:
			System.out.println("Unkown target register passed in LDR instruction");
			break;
		}
	}

	private void executeSTR(int[] R, int[] IX, int[] I, int[] address) {
		// TODO Auto-generated method stub
		// calculating effective address
		int effectiveAddress = 0;
		// copying contents of IR to EA
		int indexingRegister = Utilities.bin2dec(IX);
		switch (indexingRegister) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}
		// c(addressField)
		effectiveAddress += Utilities.bin2dec(address);// c(ir)+c(address)
		// indirect addressing
		// ea=c(c(iX)+c(addressField))

		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);// c(c(ir)+c(addressField))
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); // copies EA to MAR

		int register = Utilities.bin2dec(R);
		switch (register) {
		case 0:
			registerCopy(this.R0, this.MBR);
			break;
		case 1:
			registerCopy(this.R1, this.MBR);
			break;
		case 2:
			registerCopy(this.R2, this.MBR);
			break;
		case 3:
			registerCopy(this.R3, this.MBR);
			break;
		default:
			break;
		}
		store();
		this.I.updateDisplay();
	}

    	//copying effective address to targetRegister
	private void executeLDA(int[] R, int[] IX, int[] I, int[] address) {

		// calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		int indexingRegister = Utilities.bin2dec(IX);
		switch (indexingRegister) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}

		// indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		//copying effective address to MBR
		updateRegister("MBR", Utilities.dec2bin(effectiveAddress, 16));
		
		int targetRegister = Utilities.bin2dec(R);
		switch (targetRegister) {
		case 0:
			registerCopy(this.MBR, this.R0);
			break;
		case 1:
			registerCopy(this.MBR, this.R1);
			break;
		case 2:
			registerCopy(this.MBR, this.R2);
			break;
		case 3:
			registerCopy(this.MBR, this.R3);
			break;
		default:
			System.out.println("Unkown target register passed in LDA instruction");
            this.halted = true;
			break;
		}
	}

    //copy contents in effectiveAddress to target index register
	private void executeLDX(int[] R, int[] IX, int[] I, int[] address) {

		// calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		int indexingRegister = Utilities.bin2dec(IX);
		switch (indexingRegister) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}

		// indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		load(); // copies contents in address of MAR to MBR

		switch (indexingRegister) {
		case 1:
			registerCopy(this.MBR, this.X1);
			break;
		case 2:
			registerCopy(this.MBR, this.X2);
			break;
		case 3:
			registerCopy(this.MBR, this.X3);
			break;
		default:
			System.out.println("Unkown target register passed in LDX instruction");
            this.halted = true;
			break;
		}
	}

    //copying value from index register to effective register
	private void executeSTX(int[] R, int[] IX, int[] I, int[] address) {
		// calculating effective address
				int effectiveAddress = 0;
				// copying contents of IR to EA
				int indexingRegister = Utilities.bin2dec(IX);
				switch (indexingRegister) {
				// c(iX)
				case 0:
					break;
				case 1:
					effectiveAddress += Utilities.bin2dec(this.X1);
					break;
				case 2:
					effectiveAddress += Utilities.bin2dec(this.X2);
					break;
				case 3:
					effectiveAddress += Utilities.bin2dec(this.X3);
				default:
					System.out.println("Unknown indexing register passed");
                    this.halted = true;
				}
				// c(addressField)
				effectiveAddress += Utilities.bin2dec(address);// c(ir)+c(address)
				// indirect addressing
				// ea=c(c(iX)+c(addressField))

				if (I[0] == 1) {
					updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
					load();// mbr has c(c(ir)+c(addressField))
					effectiveAddress = Utilities.bin2dec(this.MBR);// c(c(ir)+c(addressField))
				}
				updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); // copies EA to MAR

				switch (indexingRegister) {
				case 1:
					registerCopy(this.X1, this.MBR);
					break;
				case 2:
					registerCopy(this.X2, this.MBR);
					break;
				case 3:
					registerCopy(this.X3, this.MBR);
					break;
				default:
					System.out.println("Unknown indexing register passed");
                    this.halted = true;
				}
				store();
				this.I.updateDisplay();
	}

    public void executeJZ(int[] R, int[] IX, int[] I, int[] address){
        // calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		// int indexingRegister = Utilities.bin2dec(IX);
		switch (Utilities.bin2dec(IX)) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}   

        // indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		load(); // copies contents in address of MAR to MBR
		this.I.updateDisplay();

        // int targetRegister = Utilities.bin2dec(R);
        switch(Utilities.bin2dec(R)){
            case 0:
                if (Utilities.bin2dec(this.R0) == 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            case 1:
                if (Utilities.bin2dec(this.R1) == 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;

            case 2:
                if (Utilities.bin2dec(this.R2) == 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            case 3:
                if (Utilities.bin2dec(this.R3) == 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            default:
                System.out.println("Unknown register passed");
                this.halted = true;

        }
    }

    public void executeJNE(int[] R, int[] IX, int[] I, int[] address){
        // calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		// int indexingRegister = Utilities.bin2dec(IX);
		switch (Utilities.bin2dec(IX)) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}   

        // indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		load(); // copies contents in address of MAR to MBR
		this.I.updateDisplay();

        switch(Utilities.bin2dec(R)){
            case 0:
                if (Utilities.bin2dec(this.R0) != 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            case 1:
                if (Utilities.bin2dec(this.R1) != 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;

            case 2:
                if (Utilities.bin2dec(this.R2) != 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            case 3:
                if (Utilities.bin2dec(this.R3) != 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            default:
                System.out.println("Unknown register passed");
                this.halted = true;

        }
    }

    public void executeJCC(int[] CC, int[] IX, int[] I, int[] address){
        // calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		switch (Utilities.bin2dec(IX)) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}   

        // indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		load(); // copies contents in address of MAR to MBR
		this.I.updateDisplay();
        System.out.println("input CC --> " + Arrays.toString(CC));
        int CCBit = Utilities.bin2dec(CC);
        // System.out.println("target CC --> " +targetCC);
        if (CCBit == 1){
            registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
        }
        else{
            incrementPC();
        }
    }

    public void executeJMA(int[] IX, int[] I, int[] address){
        // calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		int indexingRegister = Utilities.bin2dec(IX);
		switch (indexingRegister) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}   

        // indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}

        registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
    }

    public void executeJGE(int[] R, int[] IX, int[] I, int[] address){
        // calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		int indexingRegister = Utilities.bin2dec(IX);
		switch (indexingRegister) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}   

        // indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		load(); // copies contents in address of MAR to MBR
		this.I.updateDisplay();

        switch(Utilities.bin2dec(R)){
            case 0:
                if (Utilities.bin2dec(this.R0) >= 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            case 1:
                if (Utilities.bin2dec(this.R1) >= 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;

            case 2:
                if (Utilities.bin2dec(this.R2) >= 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            case 3:
                if (Utilities.bin2dec(this.R3) >= 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            default:
                System.out.println("Unknown register passed");
                this.halted = true;

        }
    }

    public void executeSOB(int[] R, int[] IX, int[] I, int[] address){
        // calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		// int indexingRegister = Utilities.bin2dec(IX);
		switch (Utilities.bin2dec(IX)) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}   

        // indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		load(); // copies contents in address of MAR to MBR
		this.I.updateDisplay();

        // int targetRegister = Utilities.bin2dec(R);
        switch(Utilities.bin2dec(R)){
            case 0:
                this.R0 = Utilities.dec2bin((Utilities.bin2dec(this.R0) - 1), 16);
                if (Utilities.bin2dec(this.R0) > 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            case 1:
                this.R1 = Utilities.dec2bin((Utilities.bin2dec(this.R1) - 1), 16);
                if (Utilities.bin2dec(this.R1) > 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;

            case 2:
                this.R2 = Utilities.dec2bin((Utilities.bin2dec(this.R2) - 1), 16);
                if (Utilities.bin2dec(this.R2) > 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            case 3:
                this.R3 = Utilities.dec2bin((Utilities.bin2dec(this.R3) - 1), 16);
                if (Utilities.bin2dec(this.R3) > 0){
                    registerCopy(Utilities.dec2bin(effectiveAddress, 12), this.PC);
                }
                else{
                    incrementPC();
                }
                break;
            default:
                System.out.println("Unknown register passed");
                this.halted = true;

        }
    }

    public void executeAMR(int[] R, int[] IX, int[] I, int[] address){
        // calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		// int indexingRegister = Utilities.bin2dec(IX);
		switch (Utilities.bin2dec(IX)) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}   

        // indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		load(); // copies contents in address of MAR to MBR
		this.I.updateDisplay();

        // int targetRegister = Utilities.bin2dec(R);
        switch(Utilities.bin2dec(R)){
            case 0:
                this.R0 = Utilities.dec2bin((Utilities.bin2dec(this.R0) + effectiveAddress), 16);
                break;
            case 1:
                this.R1 = Utilities.dec2bin((Utilities.bin2dec(this.R1) + effectiveAddress), 16);
                break;
            case 2:
                this.R2 = Utilities.dec2bin((Utilities.bin2dec(this.R2) + effectiveAddress), 16);
                break;
            case 3:
                this.R3 = Utilities.dec2bin((Utilities.bin2dec(this.R3) + effectiveAddress), 16);
                break;
            default:
                System.out.println("Unknown register passed");
                this.halted = true;
        }
    }

    public void executeSMR(int[] R, int[] IX, int[] I, int[] address){
        // calculating effective address
		int effectiveAddress = Utilities.bin2dec(address); // ea = c(address)
		// adding contents of IR to EA. EA = c(address) + c(IX)
		// int indexingRegister = Utilities.bin2dec(IX);
		switch (Utilities.bin2dec(IX)) {
		// c(iX)
		case 0:
			break;
		case 1:
			effectiveAddress += Utilities.bin2dec(this.X1);
			break;
		case 2:
			effectiveAddress += Utilities.bin2dec(this.X2);
			break;
		case 3:
			effectiveAddress += Utilities.bin2dec(this.X3);
		default:
			System.out.println("Unknown indexing register passed");
            this.halted = true;
		}   

        // indirect addressing
		// ea=c(c(iX)+c(addressField))
		if (I[0] == 1) {
			updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12));
			load();// mbr has c(c(ir)+c(addressField))
			effectiveAddress = Utilities.bin2dec(this.MBR);
		}
		updateRegister("MAR", Utilities.dec2bin(effectiveAddress, 12)); //// c(c(ir)+c(addressField)); copies EA to MAR
		load(); // copies contents in address of MAR to MBR
		this.I.updateDisplay();

        // int targetRegister = Utilities.bin2dec(R);
        switch(Utilities.bin2dec(R)){
            case 0:
                this.R0 = Utilities.dec2bin((Utilities.bin2dec(this.R0) - effectiveAddress), 16);
                break;
            case 1:
                this.R1 = Utilities.dec2bin((Utilities.bin2dec(this.R1) - effectiveAddress), 16);
                break;
            case 2:
                this.R2 = Utilities.dec2bin((Utilities.bin2dec(this.R2) - effectiveAddress), 16);
                break;
            case 3:
                this.R3 = Utilities.dec2bin((Utilities.bin2dec(this.R3) - effectiveAddress), 16);
                break;
            default:
                System.out.println("Unknown register passed");
                this.halted = true;
        }
    }

    public void executeAIR(int[] R, int[] address){
        if (Utilities.bin2dec(address) == 0){
            ;
        }
        else{
            switch(Utilities.bin2dec(R)){
                case 0:
                    if(Utilities.bin2dec(this.R0) == 0){
                        this.R0 = Utilities.dec2bin(Utilities.bin2dec(address), 16);
                    }
                    else{
                        this.R0 = Utilities.dec2bin((Utilities.bin2dec(this.R0) + Utilities.bin2dec(address)), 16);
                    }
                    break;
                case 1:
                    if(Utilities.bin2dec(this.R1) == 0){
                        this.R1 = Utilities.dec2bin(Utilities.bin2dec(address), 16);
                    }
                    else{
                        this.R1 = Utilities.dec2bin((Utilities.bin2dec(this.R1) + Utilities.bin2dec(address)), 16);
                    }
                    break;
                case 2:
                    if(Utilities.bin2dec(this.R2) == 0){
                        this.R2 = Utilities.dec2bin(Utilities.bin2dec(address), 16);
                    }
                    else{
                        this.R2 = Utilities.dec2bin((Utilities.bin2dec(this.R2) + Utilities.bin2dec(address)), 16);
                    }
                    break;
                case 3:
                    if(Utilities.bin2dec(this.R3) == 0){
                        this.R3 = Utilities.dec2bin(Utilities.bin2dec(address), 16);
                    }
                    else{
                        this.R3 = Utilities.dec2bin((Utilities.bin2dec(this.R3) + Utilities.bin2dec(address)), 16);
                    }
                    break;
                default:
                    System.out.println("Unknown register passed");
                    this.halted = true;
            }
        }
    }

    public void executeSIR(int[] R, int[] address){
        if (Utilities.bin2dec(address) == 0){
            ;
            }
        else{
            switch(Utilities.bin2dec(R)){
                case 0:
                    if(Utilities.bin2dec(this.R0) == 0){
                        this.R0 = Utilities.OnesComplement(Utilities.dec2bin(Utilities.bin2dec(address), 16));
                    }
                    else{
                        this.R0 = Utilities.dec2bin((Utilities.bin2dec(this.R0) - Utilities.bin2dec(address)), 16);
                    }
                    break;
                case 1:
                    if(Utilities.bin2dec(this.R1) == 0){
                        this.R1 = Utilities.dec2bin(Utilities.bin2dec(address), 16);
                    }
                    else{
                        this.R1 = Utilities.dec2bin((Utilities.bin2dec(this.R1) - Utilities.bin2dec(address)), 16);
                    }
                    break;
                case 2:
                    if(Utilities.bin2dec(this.R2) == 0){
                        this.R2 = Utilities.dec2bin(Utilities.bin2dec(address), 16);
                    }
                    else{
                        this.R2 = Utilities.dec2bin((Utilities.bin2dec(this.R2) - Utilities.bin2dec(address)), 16);
                    }
                    break;
                case 3:
                    if(Utilities.bin2dec(this.R3) == 0){
                        this.R3 = Utilities.dec2bin(Utilities.bin2dec(address), 16);
                    }
                    else{
                        this.R3 = Utilities.dec2bin((Utilities.bin2dec(this.R3) - Utilities.bin2dec(address)), 16);
                    }
                    break;
                default:
                    System.out.println("Unknown register passed");
                    this.halted = true;
            }
        }
    }

    public void executeRFS(int[] address){
        this.R0 = Utilities.dec2bin(Utilities.bin2dec(address), 16);
        this.PC = Utilities.dec2bin(Utilities.bin2dec(this.R3), 12);
    }

    public void executeTRR(int[] R, int[] IX){
        
        int[] targetRegister = new int[16];
        int[] targetIndexRegister = new int[16];

        switch(Utilities.bin2dec(R)){
            case 0:
                targetRegister = this.R0;
            break;
            case 1:
                targetRegister = this.R1;
            break;
            case 2:
                targetRegister = this.R2;
                break;
            case 3:
                targetRegister = this.R3;
                break;
            default:
                System.out.println("Unknown register passed");
                this.halted = true;
        }

        switch(Utilities.bin2dec(IX)){
            case 1:
                targetIndexRegister = this.X1;
                break;
            case 2:
                targetIndexRegister = this.X2;
                break;
            case 3:
                targetIndexRegister = this.X3;
                break;
            default:
                System.out.println("Unknown register passed");
                this.halted = true;
        }

        System.out.println("R" + Utilities.bin2dec(R) + " --> "+Arrays.toString(targetRegister));
        System.out.println("IX" + Utilities.bin2dec(IX) + " --> "+Arrays.toString(targetIndexRegister));
        
        if (Utilities.bin2dec(targetRegister) == Utilities.bin2dec(targetIndexRegister)){
            this.CC = Utilities.dec2bin(1, 4);
        }
        else{
            this.CC = Utilities.dec2bin(0, 4);

        }
    }


    /**
     * Loads from memory the contents at the address specified by the MAR into the MBR.
     */
    public void load() {
        int[] v = this.M.get(Utilities.bin2dec(this.MAR));
        for (int i = 0; i < this.MBR.length; i++) {
            this.MBR[i] = v[i];
        }
        // Notify the interface that changes may have been made
        this.I.updateDisplay();
    }

    /**
     * Stores in memory the contents in MBR at the address specified by the MAR.
     */
    public void store() {
        this.M.set(Utilities.bin2dec(this.MAR), this.MBR);
        // Notify the interface that changes may have been made
        this.I.updateDisplay();
    }

    /**
     * allows you to choose file to load into memory.
     */
    public void init() {
        try{
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = new File(fileChooser.getSelectedFile().getAbsolutePath());
                FileInputStream fstream = new FileInputStream(selectedFile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                int[] MBR_INIT = new int[16];
                int count = 0; 

                while ((strLine = br.readLine()) != null)   {
                    String[] tokens = strLine.split(" ");

                    MBR_INIT = Utilities.hex2bin(tokens[1], 16);
                    MAR_INIT[count] = Integer.parseInt(tokens[0], 16);
                    count += 1;
                    lines += 1;
                    
                    this.M.set(Integer.parseInt(tokens[0], 16), MBR_INIT);
                }           
                in.close();
                System.out.println(this.M);
            }
        }catch (Exception err){
              System.err.println("Error: " + err.getMessage());
            }
    }
    /**
     * allows you to choose file to load into memory.
     */
    public void run() {
        halted = false;
        while (!this.halted) {
            this.step();
            this.I.updateDisplay();
        }
        System.out.println("ran until halted...");
    }

    /**
     * Sets the register with name n to have the value v.
     * 
     * @param name the name of the register having its value set
     * @param v the array of integers (each of which should be zero or one) that this register is being set to
     */
    public void updateRegister(String name, int[] v) {
        // Check that v is in fact just zeros and ones
        for (int i = 0; i < v.length; i++) {
            if (v[i] != 0 && v[i] != 1) {
                System.out.println("the " + i + "th value of v in the set function of Simulator.java is " + v[i] + " but should be zero or one only");
                this.halted = true;
            }
        }

        // Switch over the name of the register
        switch (name) {
            case "R0":
                for (int i=0; i<this.R0.length; i++) this.R0[i] = v[i];
                break;
            case "R1":
                for (int i=0; i<this.R1.length; i++) this.R1[i] = v[i];
                break;
            case "R2":
                for (int i=0; i<this.R2.length; i++) this.R2[i] = v[i];
                break;
            case "R3":
                for (int i=0; i<this.R3.length; i++) this.R3[i] = v[i];
                break;
            case "PC":
                for (int i=0; i<this.PC.length; i++) this.PC[i] = v[i];
                break;
            case "CC":
                for (int i=0; i<this.CC.length; i++) this.CC[i] = v[i];
                break;
            case "IR":
                for (int i=0; i<this.IR.length; i++) this.IR[i] = v[i];
                break;
            case "MAR":
                for (int i=0; i<this.MAR.length; i++) this.MAR[i] = v[i];
                break;
            case "MBR":
                for (int i=0; i<this.MBR.length; i++) this.MBR[i] = v[i];
                break;
            case "X1":
                for (int i=0; i<this.X1.length; i++) this.X1[i] = v[i];
                break;
            case "X2":
                for (int i=0; i<this.X2.length; i++) this.X2[i] = v[i];
                break;
            case "X3":
                for (int i=0; i<this.X3.length; i++) this.X3[i] = v[i];
                break;
            default:
                System.out.println("In the updateRegister function in the Simulator class recieved string: "+name);
                this.halted = true;
        }
    }

    /**
     * Returns the value of the register with name name.
     * 
     * @param name the name of the register whose value is returned
     * @return an int array of only zeros and ones giving the value of the register of name name
     */
    public int[] getRegister(String name) {
        // Switch over the name of the register
        switch (name) {
            case "R0":
                return R0;
            case "R1":
                return R1;
            case "R2":
                return R2;
            case "R3":
                return R3;
            case "PC":
                return PC;
            case "CC":
                return CC;
            case "IR":
                return IR;
            case "MAR":
                return MAR;
            case "MBR":
                return MBR;
            case "MFR":
                return MFR;
            case "X1":
                return X1;
            case "X2":
                return X2;
            case "X3":
                return X3; 
            default:
                System.out.println("In the getRegister function in the Simulator class recieved string: "+name);
                this.halted = true;
        }
        int[] r = {-1};
        return r;
    }

    /**
     * Increments the PC.
     */
    public void incrementPC() {
        int d = Utilities.bin2dec(this.PC) + 1;
        if (d >= Math.pow(2,this.PC.length)) {
            d = 0;//TODO figure out what actually should be done in this case.
        }
        int[] v = Utilities.dec2bin(d, this.PC.length);
        for (int i = 0; i < this.PC.length; i++) {
            this.PC[i] = v[i];
        }
        System.out.println(Arrays.toString(this.PC));
    }

    /**
     * Gives a reference to an interface object so that this simulator can update/notify the interface when changes occur and it can update accordingly.
     * 
     * @param I the interface object being given to this simulator
     */
    public void giveInterface(Interface I) {
        this.I = I;
    }

    /**
     * Copies the value from the from register to the to register.
     * 
     * @param from the register from which a value is being copied
     * @param to the register to which a value is being copied
     */
    public void registerCopy(int[] from, int[] to) {
        // Check that the two registers are of the same length
        if (from.length != to.length) {
            System.out.println("in registerCopy of Simulator, from has length "+from.length+" whereas to has length "+to.length+" but they should be the same");
            this.halted = true;
            return;
        }
        // Check that the value being copied consists only of ones and zeros (as it should)
        for (int i = 0; i < from.length; i++) {
            if (from[i] != 0 && from[i] != 1) {
                System.out.println("in registerCopy of Simulator, from contains a "+from[i]+" at position "+i+" but should conly contain ones and zeros");
                this.halted = true;
                return;
            }
        }
        // Perform the copy
        for (int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }
}
