package processor;
import processing.core.*;
import io.Main;

public class Processor extends Memory {
	int cycle = 0x00;
	int currCycle = 0x00;
	AddressModes currMode;
	OpCodes currOp;
	byte instructionLen;
	boolean running = true;
	PApplet main;
	
	//Takes instruction, resolves start and destination, sends and recieves result from opperations, and sends to destination 
	public Processor(PApplet main) {
		this.main = main;
	}
	
	public void exec() {
		currOp = opMap[getByte(getPC())];
		currMode = modeMap[getByte(getPC())];
		executeInstruction();
		//System.out.println(getReg(Registers.X));
		setPC(getPC() + instructionLen);
		display();
	}
	
	private void display() {
		main.fill(0);
		for (int y = 0; y < Main.length; y++) {
			for (int x = 0; x < Main.length; x++) {
				main.fill(getByte((y * 16) + x));
				main.square(x * Main.size, y * Main.size, Main.size);
			}
		}
	}
	
	void nextOp() {
		currMode = modeMap[getByte(getPC())];
		currOp = opMap[getByte(getPC())];
		try {
			executeInstruction();
		} catch (Exception e) {
			System.out.print("\n\nBad OPcode: 0x" + Integer.toHexString(getByte(getPC())) + "\n");
			throw e;
		}
		System.out.print("\nProgram Counter: 0x" + Integer.toHexString(getPC()) + "\n");
		System.out.print("Instruction at PC address: 0x" + Integer.toHexString((getByte(getPC()) << 8) + getByte(getPC() + 1)) + "\n");
		System.out.print("Opcode: " + opMap[getByte(getPC())] + "\n");
		System.out.print("Address Mode: " + modeMap[getByte(getPC())] + "\n");
		System.out.print("Register SR after execution: " + Integer.toString(getReg(Registers.SR), 2) + "\n");
	}
	
	void executeInstruction() {
		switch(currOp) {
		
		//Load and store ops.
		case LDA: case LDX: case LDY: case STA: case STX: case STY:
			loadAndStore(addresser());
			break;
		
		//Arithmatic ops.
		case ADC: case SBC:
			arithmetic(addresser());
			break;
		
		//Increment and decrement ops.
		case INC: case INX: case INY: case DEC: case DEX: case DEY:
			incAndDec(addresser());
		 	break;
			
		//Shift and rotate ops.
		case ASL: case LSR: case ROL: case ROR:
			shiftAndRotate(addresser());
			break;
			
		//Logic ops.
		case AND: case ORA: case EOR:
			logic(addresser());
			break;
			
		//Compare and test bit ops.
		case CMP: case CPX: case CPY: case BIT:
			compAndTest(addresser());
			break;
			
		//Branch ops.
		case BCC: case BCS: case BNE: case BEQ: case BPL: case BMI: case BVC: case BVS:
			branch(addresser());
			break;
			
		//Transfer ops.
		case TAX: case TXA: case TAY: case TYA: case TSX: case TXS:
			transfer();
			break;
			
		//Stack ops.
		case PHA: case PLA: case PHP: case PLP:
			stack();
			break;
			
		//Subroutines and jump ops.
		case JMP: case JSR: case RTS: case RTI:
			jumpAndSub(addresser());
			break;
			
		//Set and clear ops.
		case CLC: case SEC: case CLD: case SED: case CLI: case SEI: case CLV: 
			setAndClear();
			break;
		case BRK: case NOP:
			breakAndNop();
			
		default: return;
		}
	}
	
	//Returns address ([0]) and value at address ([1]) depending on addressing mode.
	//Direct value will always return address 0xFFFF, this should never be used.
	private int[] addresser() {
		switch(currMode) {
		case I:
			instructionLen = 1;
		case A:
			instructionLen = 1;
			return new int[] {getReg(Registers.A)};
		case IM:
			instructionLen = 2;
			return new int[] {0xFFFF, getByte(getPC() + 1)};
		case AB:
			instructionLen = 3;
			return new int[] {getTwoByte(getPC() + 1), getByte(getTwoByte(getPC() + 1))};
		case ZP:
			instructionLen = 2;
			return new int[] {getByte(getPC() + 1), getByte(getByte(getPC() + 1))};
		case R:
			instructionLen = 3;
			return new int[] {getPC() + getByte(getPC() + 1), getByte(getPC() + getByte(getPC() + 1))};
		case AI:
			instructionLen = 3;
			return new int[] {getTwoByte(getByte(getPC() + 1)), getByte(getTwoByte(getByte(getPC() + 1)))};
		case AIX:
			instructionLen = 3;
			return new int[] {getTwoByte(getPC() + 1) + getReg(Registers.X), getByte(getTwoByte(getPC() + 1) + getReg(Registers.X))};
		case AIY:
			instructionLen = 3;
			return new int[] {getTwoByte(getPC() + 1) + getReg(Registers.Y), getByte(getTwoByte(getPC() + 1) + getReg(Registers.Y))};	
		case ZPX:
			instructionLen = 2;
			return new int[] {getReg(Registers.X) + getByte(getPC() + 1), getByte(getReg(Registers.X) + getByte(getPC() + 1))};
		case ZPY:
			instructionLen = 2;
			return new int[] {getReg(Registers.Y) + getByte(getPC() + 1), getByte(getReg(Registers.Y) + getByte(getPC() + 1))};
		case ZPIX:
			instructionLen = 2;
			return new int[] {getTwoByte(getReg(Registers.X) + getByte(getPC() + 1)), getByte(getTwoByte(getReg(Registers.X) + getByte(getPC() + 1)))};
		case ZPIY:
			instructionLen = 2;
			return new int[] {getReg(Registers.Y) + getTwoByte(getPC() + 1), getByte(getReg(Registers.Y) + getTwoByte(getPC() + 1))};
		default: return new int[] {0, 0};
		}
	}
	
	//Sets flags for zero and negative values
	private void setZNFlags(byte regVal) {
		//Zero.
		if (regVal == 0) setReg(Registers.SR, (short)(getReg(Registers.SR) | 0b10));
		//Negative.
		if ((regVal >>> 7) == 1) setReg(Registers.SR, (short)(getReg(Registers.SR) | (1 << 7)));
	}
	
	private void loadAndStore(int[] address) {
		switch(currOp) {
		case LDA:
			setReg(Registers.A, (byte)address[1]);
			setZNFlags((byte)getReg(Registers.A));
			break;
		case LDX:
			setReg(Registers.X, (byte)address[1]);
			setZNFlags((byte)getReg(Registers.X));
			break;
		case LDY:
			setReg(Registers.Y, (byte)address[1]);
			setZNFlags((byte)getReg(Registers.Y));
			break;
		case STA:
			setByte(address[0], getReg(Registers.A));
			break;
		case STX:
			setByte(address[0], getReg(Registers.X));
			break;
		case STY:
			setByte(address[0], getReg(Registers.Y));
			break;
		default: break;
		}
	}
	
	private void arithmetic(int[] address) {
		int set = 0;
		int signA = Integer.signum((byte)getReg(Registers.A));
		int signM = Integer.signum((byte)address[1]);
		switch(currOp) {
		case ADC:
			set = address[1] + getReg(Registers.A) + (getReg(Registers.SR) & 0x1);
			setReg(Registers.SR, (short)(getReg(Registers.SR) | (set >>> 8)));
			setReg(Registers.A, (byte)set);
			break;
		case SBC:
			set = (address[1] ^ 0xff) + getReg(Registers.A) + (getReg(Registers.SR) & 0x1);
			setReg(Registers.SR, (short)(getReg(Registers.SR) | (set >>> 8)));
			setReg(Registers.A, (byte)set);
			break;
		default:
			break;
		}
		setZNFlags((byte)getReg(Registers.A));
		set = Integer.signum((byte)set);
		if ((set != signA) && (set != signM)) setReg(Registers.SR, (short)(getReg(Registers.SR) | (1 << 6)));
	}
	
	private void incAndDec(int[] address) {
		instructionLen = 1;
		switch(currOp) {
		case INC:
			instructionLen = 2;
			setByte(address[0], (short)(address[1] + 1));
			setZNFlags((byte)address[1]);
			break;
		case DEC:
			instructionLen = 2;
			setByte(address[0], (short)(address[1] - 1));
			setZNFlags((byte)address[1]);
			break;
		case INX:
			setReg(Registers.X, (short)(getReg(Registers.X) + 1));
			setZNFlags((byte)getReg(Registers.X));
			break;
		case DEX:
			setReg(Registers.X, (short)(getReg(Registers.X) - 1));
			setZNFlags((byte)getReg(Registers.X));
			break;
		case INY:
			setReg(Registers.Y, (short)(getReg(Registers.Y) + 1));
			setZNFlags((byte)getReg(Registers.Y));
			break;
		case DEY:
			setReg(Registers.Y, (short)(getReg(Registers.Y) - 1));
			setZNFlags((byte)getReg(Registers.Y));
			break;
		default: return;
		}
	}
	
	private void shiftAndRotate(int[] address) {
		
		int set = 0;
		int value = 0;
		//Checks length of address array; any length less that 2 will mean that accumulator will be used.
		if (address.length < 2) {
			value = address[0];
		} else {
			value = address[1];
		}
		
		switch(currOp) {
		case ASL:
			setReg(Registers.SR, (short)(getReg(Registers.SR) | (value >>> 7)));
			set = (byte)(value << 1);
			break;
		case LSR:
			setReg(Registers.SR, (short)(getReg(Registers.SR) | (value & 0x1)));
			set = (byte)(value >>> 1);
			break;
		case ROL:
			set = (byte)((value << 1) + (value >>> 7));
			break;
		case ROR:
			setReg(Registers.SR, (short)(getReg(Registers.SR) | (value & 0x1)));
			set = (byte)((value >>> 1) + ((value & 0x1) << 7));
			break;
		default: break;
		}
		
		if (address.length < 2) {
			setReg(Registers.A, (byte)set);
		} else {
			setByte(address[0], (byte)set);
		}
	}
	
	private void logic(int[] address) {
		switch(currOp) {
		case AND:
			setReg(Registers.A, (short)(getReg(Registers.A) & address[1]));
			break;
		case ORA:
			setReg(Registers.A, (short)(getReg(Registers.A) | address[1]));
			break;
		case EOR:
			setReg(Registers.A, (short)(getReg(Registers.A) ^ address[1]));
			break;
		default: break;
		}
		setZNFlags((byte)getReg(Registers.A));
	}
	
	private void compAndTest(int[] address) {
		int regVal = 0;
		switch (currOp) {
		case CMP:
			regVal = getReg(Registers.A);
			break;
		case CPX:
			regVal = getReg(Registers.X);
			break;
		case CPY:
			regVal = getReg(Registers.Y);
			break;
		case BIT:
			int compVal = getReg(Registers.A) & address[1];
			setReg(Registers.SR, (byte)(getReg(Registers.SR) | (compVal & (0b11 << 6))));
			return;
		default: break;
		}

		if((byte)regVal < (byte)address[1]) {
			setReg(Registers.SR, (byte)(getReg(Registers.SR) | (1 << 7)));
		} else if((byte)regVal > (byte)address[1]) {
			setReg(Registers.SR, (byte)(getReg(Registers.SR) | 1));
		} else {
			setReg(Registers.SR, (byte)(getReg(Registers.SR) | (0b11)));
		}
	}
	
	private void branch(int[] value) {
		instructionLen = 2;
		switch (currOp) {
		case BCC:
			if ((getReg(Registers.SR) & 1) == 0) setPC(value[0]);
			break;
		case BCS:
			if ((getReg(Registers.SR) & 1) == 1) setPC(value[0]);
			break;
		case BNE:
			if (((getReg(Registers.SR) >>> 1) & 1) == 0) setPC(value[0]);
			break;
		case BEQ:
			if (((getReg(Registers.SR) >>> 1) & 1) == 1) setPC(value[0]);
			break;
		case BPL:
			if (((getReg(Registers.SR) >>> 7) & 1) == 0) setPC(value[0]);
			break;
		case BMI:
			if (((getReg(Registers.SR) >>> 7) & 1) == 1) setPC(value[0]);
			break;
		case BVC:
			if (((getReg(Registers.SR) >>> 6) & 1) == 0) setPC(value[0]);
			break;
		case BVS:
			if (((getReg(Registers.SR) >>> 6) & 1) == 1) setPC(value[0]);
			break;
		default: return;
		}
	}
	
	private void transfer() {
		switch (currOp) {
		case TAX:
			setReg(Registers.X, getReg(Registers.A));
		case TXA:
			setReg(Registers.A, getReg(Registers.X));
		case TAY:
			setReg(Registers.Y, getReg(Registers.A));
		case TYA:
			setReg(Registers.A, getReg(Registers.Y));
		case TSX:
			setReg(Registers.X, getReg(Registers.SP));
		case TXS:
			setReg(Registers.SP, getReg(Registers.X));
		default: break;
		}
		instructionLen = 1;
	}
	 
	private void stack() {
		switch (currOp) {
		case PHA:
			setByte(getReg(Registers.SP), getReg(Registers.A));
			setReg(Registers.SP, (short)(getReg(Registers.SP) - 1));
		case PLA:
			setReg(Registers.A, getByte(getReg(Registers.SP)));
			setByte(getReg(Registers.SP), (short)0);
			setReg(Registers.SP, (short)(getReg(Registers.SP) + 1));
		case PHP:
			setByte(getReg(Registers.SP), getReg(Registers.SR));
			setReg(Registers.SP, (short)(getReg(Registers.SP) - 1));
		case PLP:
			setReg(Registers.SR, getByte(getReg(Registers.SP)));
			setByte(getReg(Registers.SP), (short)0);
			setReg(Registers.SP, (short)(getReg(Registers.SP) + 1));
		default: break;
		}
	}
	
	private void jumpAndSub(int[] address) {
		instructionLen = 1;
		switch (currOp) {
		case JMP:
			setPC(address[0] - 1);
			break;
		case JSR:
			setPC(address[0] - 1);
			int retVal = getPC();
			setByte(getReg(Registers.SP), (short)(retVal >>> 8));
			setByte(getReg(Registers.SP) - 1, (short)(retVal & 0xf));
			setReg(Registers.SP, (short)(getReg(Registers.SP) - 2));
			break;
		case RTS:
			setPC((getByte(getPC() + 2) << 8) + getByte(getPC() + 1));
			setByte(getByte(getPC() + 2), (short)0);
			setByte(getByte(getPC() + 1), (short)0);
			setReg(Registers.SP, (short)(getReg(Registers.SP) + 2));
			break;
		case RTI:
			setReg(Registers.SR, getByte(getReg(Registers.SP) + 1));
			setPC((getByte(getPC() + 3) << 8) + getByte(getPC() + 2));
			setByte(getByte(getPC() + 1), (short)0);
			setByte(getByte(getPC() + 2), (short)0);
			setByte(getByte(getPC() + 3), (short)0);
			setReg(Registers.SP, (short)(getReg(Registers.SP) + 3));
			break;
		default: break;
		}
	}
	
	private void setAndClear() {
		switch (currOp) {
		case CLC:
			setReg(Registers.SR, (short)(getReg(Registers.SR) & 0));
		case SEC:
			setReg(Registers.SR, (short)(getReg(Registers.SR) | 1));
		case CLI:
			setReg(Registers.SR, (short)(getReg(Registers.SR) & 0xdf));
		case SEI:
			setReg(Registers.SR, (short)(getReg(Registers.SR) | ~0xdf));
		case CLV:
			setReg(Registers.SR, (short)(getReg(Registers.SR) & 0xfd));
		default: break;
		}
	}
	
	private void breakAndNop() {
		switch (currOp) {
		case BRK:
			setReg(Registers.SR, (short)(getReg(Registers.SR) | ~0xf7));
			setReg(Registers.SR, (short)(getReg(Registers.SR) | ~0xdf));
		case NOP:
			break;
		default: break;
		}
	}
	
}


