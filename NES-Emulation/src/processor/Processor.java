package processor;


public class Processor extends Memory {
	int cycle = 0x00;
	int currCycle = 0x00;
	AddressModes currMode;
	OpCodes currOp;
	byte instructionLen;
	
	//Takes instruction, resolves start and destination, sends and recieves result from opperations, and sends to destination 
	public Processor() {
		nextOp();
	}
	
	
	void nextOp() {
		setByte(getPC(), (short)0x9e);
		setByte(getPC() + 1, (short)0x11);
		setReg(Registers.SR, (short)0x0);
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
		System.out.print("Register X after execution: 0x" + Integer.toHexString(getReg(Registers.X)) + "\n");
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
		 	break;
			
		//Shift and rotate ops.
		case ASL: case LSR: case ROL: case ROR:
			break;
			
		//Logic ops.
		case AND: case ORA: case EOR:
			break;
			
		//Compare and test bit ops.
		case CMP: case CPX: case CPY: case BIT:
			break;
			
		//Branch ops.
		case BCC: case BCS: case BNE: case BEQ: case BPL: case BMI: case BVC: case BVS:
			break;
			
		//Transfer ops.
		case TAX: case TXA: case TAY: case TYA: case TSX: case TXS:
			break;
			
		//Stack ops.
		case PHA: case PLA: case PHP: case PLP:
			break;
			
		//Subroutines and jump ops.
		case JMP: case JSR: case RTS: case RTI:
			break;
			
		//Set and clear ops.
		case CLC: case SEC: case CLD: case SED: case CLI: case SEI: case CLV: case BRK: case NOP:
			break;
		}
	}
	
	//Returns value dependent on current address mode.
	//Return may be a value or address; this willl be dependent on OPcode and does not need to be defined as such. cv 
	private int addresser() {
		switch(currMode) {
		case IM:
			instructionLen = 2;
			return getByte(getPC() + 1);
		case AB:
			instructionLen = 3;
			return getByte(getTwoByte(getPC() + 1));
		case ZP:
			instructionLen = 2;
			return getByte(getByte(getPC() + 1));
		case R:
			instructionLen = 3;
			return getPC() + (byte)getByte(getPC() + 1);
		case AI:
			instructionLen = 3;
			return getTwoByte(getByte(getPC() + 1));
		case AIX:
			instructionLen = 3;
			return getByte(getTwoByte(getPC() + 1) + getReg(Registers.X));
		case AIY:
			instructionLen = 3;
			return getByte(getTwoByte(getPC() + 1) + getReg(Registers.Y));	
		case ZPX:
			instructionLen = 2;
			return getByte(getReg(Registers.X) + getByte(getPC() + 1));
		case ZPY:
			instructionLen = 2;
			return getByte(getReg(Registers.Y) + getByte(getPC() + 1));
		case ZPIX:
			instructionLen = 2;
			return getByte(getTwoByte(getReg(Registers.X) + getByte(getPC() + 1)));
		case ZPIY:
			instructionLen = 2;
			return getReg(Registers.Y) + getTwoByte(getPC() + 1);
		default:
			return 0;
		}
	}
	
	private void loadAndStore(int value) {
		switch(currOp) {
		case LDA:
			setReg(Registers.A, (byte)value);
			break;
		case LDX:
			setReg(Registers.X, (byte)value);
			break;
		case LDY:
			setReg(Registers.Y, (byte)value);
			break;
		case STA:
			setByte(value, getReg(Registers.A));
			break;
		case STX:
			setByte(value, getReg(Registers.X));
			break;
		case STY:
			setByte(value, getReg(Registers.Y));
			break;
		default:
			break;
		}
	}
	
	private void arithmetic(int value) {
		int set = 0;
		switch(currOp) {
		case ADC:
			set = value + getReg(Registers.A) + (getReg(Registers.SR) & 0x1);
			setReg(Registers.SR, (short)(getReg(Registers.SR) | (set >>> 8)));
			setReg(Registers.A, (byte)set);
			break;
		case SBC:
			set = (value ^ 0xff) + getReg(Registers.A) + (getReg(Registers.SR) & 0x1);
			setReg(Registers.SR, (short)(getReg(Registers.SR) | (set >>> 8)));
			setReg(Registers.A, (byte)set);
			break;
		default:
			break;
		}
	}
}


