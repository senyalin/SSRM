package A;

import java.io.*;


public class BitIO{
	int byteArray[];		//放壓縮後的 byte， Ps：前提假設能壓縮比原檔還小
	int byteN,bitN;			//記錄儲存幾個 byte 
	int bytePt, bitPt;		//讀入時記錄目前應讀取的是第幾個位元、第幾位元 7->0 %8
	final int Power2[] = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024 };

	public BitIO(int size) {
		byteArray=new int [size];
		for (int i=0; i<byteArray.length; i++)
			byteArray[i]=0;
		init();
	}
	public void init() {
		byteN=0;
		bitN=7;
		bytePt=0;
		bitPt=7;
	}

//==================================== Output ==================================
	public void outFile(String iofile) {
		try {
			FileOutputStream os = new FileOutputStream(iofile);
			int byteLength= byteN;
			if (bitN!=7) byteLength++;
			for (int i=0;i<byteLength ;i++)
				os.write(byteArray[i]);
			os.close();
		}
		catch (Exception e) { System.out.println(e); }
	}
	
	public void appendToBitStream(int i, int bitsLength){	// bitsLength <32
		outBits(i,bitsLength);
	}
	//將BIT轉換BYTE
	public void outBits(int value,int bitsLength){ //轉換成 Byte放入 byteArray 陣列
		int i, bit;
		for (i=bitsLength-1; i>=0; i--){
			bit= (value & Power2[i])>>i;
			outBit(bit);
		}
	}
	public boolean outBit(int bit){			//將壓縮後的資料轉換成 Byte 放入 byteArray 陣列
		if (byteN==byteArray.length) return false;
		if (bit%2==1)
			byteArray[byteN] = byteArray[byteN] | Power2[bitN];
		if (bitN==0) { bitN=7; byteN++; }
				else { bitN--; }
		return true;

	}

//==================================== Input ==================================
	public void inFile(String iofile) {
		try {
			FileInputStream is = new FileInputStream(iofile);
			int ch;
			for (byteN=0; (ch=is.read())!=-1; byteN++)
				byteArray[byteN]=ch;
			is.close();
			bytePt=0; bitPt=7; bitN=7;
		}
		catch (Exception e) { System.out.println(e); }
	}
	
	public int extractFromBitStream(int bitsLength) {
		int sum=0, bit;
		for (int i=0; i<bitsLength; i++) {
			if ((bit= inBit())!=-1) { sum= sum<<1 | bit; }
							   else { sum=-sum; break; }
		}
		return sum;
	}
	
	public int inBit() {
		if ((byteN==bytePt) && (bitPt==bitN)) return -1;	// EObits
		int r;
		if ((byteArray[bytePt] & Power2[bitPt])==0) r=0; else r=1;
		if (bitPt==0) { bitPt=7; bytePt++; }
				 else { bitPt--; }
		return r;
	}

//==================================== Other ==================================
    //十進轉二進
	/*
	public String intToBitString(int x,int y){		
		int i;
		String n="";
		for (i=y-1; i>=0; i--){
			n=n+""+((int)(x/Power2[i]));
			x=(int)(x % Power2[i]);
		}
		return n;
	}*/

}

