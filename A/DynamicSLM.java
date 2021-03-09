package A;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class DynamicSLM {

	static String path = "";					 					//���ɪ����|
	static String fileName[] = {"Baboon", "Barbara", "Boats", "GoldHill", 
		     					"Jet_F16", "Lena", "Pepper", "SailBoat", "Tiffany"};			 		
																	//���ɪ��ɦW
	
	final int MAXVAL=255;											//�]�w��m�۫�
	static String inputImgName, outputImgName;						//��J���X���ɦW
	static String infoFileName, extractFileName;	
	static int imgHeight = 512, imgWidth = 512;	 					//�Ϫ����M�e
	static int imgArray[][] = new int[imgHeight+1][imgWidth+1];  	//Original Image���}�C
	static int stegoArray[][] = new int[imgHeight+1][imgWidth+1];	//Stego Image���}�C
	static int matrixSize = 256;									//�ƿW�}�C���j�p(pixel�Ȱ�)
	static int sudokuMap[][] = new int[matrixSize][matrixSize];
	static byte infoArray[];										//���ø�T���}�C
	static int digitArray[];										//���K�Ʀr���}�C
	static int digitNum = 87382;									//���K�Ʀr���ƶq
	static int extractArray[];										//���X��T���}�C
	static int picNo = 0;											//���ռv�����s��
	static int bitLength = 0;										//���ø�T������
	static int L = 0;												//Matrix���j�p
	BitIO bitObj;
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		for(picNo = 5; picNo<6; picNo++){
			inputImgName = path + "\\RawImage\\" + fileName[picNo] + ".raw";
			@SuppressWarnings("unused")						//�q�X Cover Image
			ShowRaw picObj1 = new ShowRaw(inputImgName,imgWidth,imgHeight,"SLM System:"+fileName[picNo]);
			for(L = 16; L<=16; L++){
				System.out.println("["+fileName[picNo]+".raw"+"/] (L = "+L+")");
				
				outputImgName = path + "\\StegoImage\\" + fileName[picNo] + "_STEGO(L = " +L+ ").raw";
				
				infoFileName = path + "\\SecretData\\" + "info(L = " +L+ ").dat";
				
				extractFileName = path + "\\ExtractedData\\" + "info(L = " +L+ ").dat";
				
				DynamicSLM obj = new DynamicSLM();
				
				
				//================================== Embeding phase ===================================
				obj.readImageFile(inputImgName, imgArray);		//Ū�� Original Image
				//obj.imageAnalysis();
				obj.computeCapacity(L*L);
				obj.RandomInfo(0, L*L);
				obj.countInfoUnit(L*L);
				obj.outputInfo(infoFileName, digitArray);
				obj.createSLM(0, L*L);							//�ѼƬO(seed, T)
				//obj.printMatrix(9*9);
				obj.hideInfo();
				obj.diffCompare();
				obj.printInfo(10);
				obj.writeImageFile(outputImgName, stegoArray);	//��X Stego Image
				
				//================================== Extracting phase ===================================
				obj.readImageFile(outputImgName, stegoArray);	//Ū�� Stego Image
				obj.countInfoUnit(L*L);
				obj.createSLM(0, L*L);
				obj.extractInfo(extractFileName);
				obj.compareTwoFiles(infoFileName, extractFileName);
				System.out.println("[/"+fileName[picNo]+".raw"+"]");
				System.out.println();
				@SuppressWarnings("unused")						//�q�X Stego Image
				ShowRaw picObj2 = new ShowRaw(outputImgName,imgWidth,imgHeight,"SLM System:"+fileName[picNo]+" (L = "+L+")");
			}
		}
	}

	//--------------------------------------------------------- �ɮ�IO
	public void readImageFile(String fn, int picArray[][]) {		//Ū��
		try {
			
			InputStream is = new FileInputStream(fn);
		
			int pixel;	//������
			
			for (int i=0; i<imgHeight; i++ ){		//�N������Ū�i�}�C��
				for (int j=0; j<imgWidth; j++)
					if ((pixel = is.read()) != -1){
						picArray[i][j] = pixel;
					}
			}	
			is.close();
		}
		catch (IOException e) { System.out.println(e); }
	}
	
	
	public void writeImageFile(String fn, int picArray[][]) {		//�g��
		try {
			OutputStream os = new FileOutputStream(fn);	

			for (int i=0; i<imgHeight; i++ )		//�N�����ȼg�J�ɮ׸�
				for (int j=0; j<imgWidth; j++)
					os.write(picArray[i][j]);
			os.close();
		}
		catch (IOException e) { System.out.println(e); }
	}
	
	
	//-----------------------------------------------------------------
	
	public void imageAnalysis(){
		int upCount = 0, lowCount = 0, midCount = 0;
		
		
		for(int i=0; i<imgHeight; i++){
			for(int j=0; j<imgWidth; j++){
				if(imgArray[i][j] == 0){ lowCount++; }
				else if(imgArray[i][j] == 255){ upCount++; }
				else{ midCount++; }
			}
		}
		
		System.out.println("Upper Boundary [255]: "+upCount+" pixels");
		System.out.println("Lower Boundary [0]: "+lowCount+" pixels");
		System.out.println("Between Boundary [0-255]: "+midCount+" pixels");
		System.out.println("Total spots: "+(lowCount+midCount+upCount)+" pixels");
	}
	
	public void computeCapacity(int size){
		double result = Math.log10(size)/Math.log10(2);	 //�p���T�öq
		result = result / 2;
		
		System.out.println("��T�öq: "+result+" bpp");
	}
	
	public void RandomInfo(int seed, int size){	//create a random secret data
		
		digitArray = new int[digitNum];		
		Random rd = new Random(seed);  // �A������m�ؤl
			
		int T = (int)Math.ceil(Math.log10(size) / Math.log10(2));
		int bitNum = digitNum*T;
		infoArray = new byte[bitNum];
		int n = 0, digit = 0;
		
		for(int i=0;i<bitNum;i++){
			infoArray[i] = (byte)rd.nextInt(2); //get random for 0 or 1
		}
		
		for(int i=0; i<digitNum; i++){
			digit = 0;
			for(int j=T-1; j>=0; j--){
				if(infoArray[n] == 1){
					digit += Math.pow(2, j);
				}
				n++;
			}
			if(digit >= size){
				digitArray[i] = digit >> 1;
				n--;
			}
			else{
				digitArray[i] = digit;
			}
			
		}
	}
	
	public void countInfoUnit(int dividend){
		bitLength = 0;		//Reset bitLength counter
		while((dividend = dividend/2) != 0){ bitLength++; }
		//System.out.println("****"+bitLength);
	}
	
	public void outputInfo(String fn, int info[]){		// create a random information	
		BitIO bitObj = new BitIO(imgHeight*imgWidth);
	
		for(int i=0;i<digitNum;i++){
			if(info[i] >= Math.pow(2, bitLength)){
				bitObj.appendToBitStream(info[i], bitLength+1);
			}
			else{
				bitObj.appendToBitStream(info[i], bitLength);
			}
		}
		
		bitObj.outFile(fn);
	}
	
	
	public void createSLM(int seed, int size){		// create a random information
			
			Random rd = new Random(seed);  // �A������m�ؤl
			boolean Num[] = new boolean[size];
			int Sudoku[] = new int[size];
			boolean finishSudoku = false;
			int current = 0;
			int numCount = 0;
			int edge = (int)Math.sqrt(size);
			//int check =0 ;
			
			
			for (int i=0; i<size; i++)
					Num[i] = false;
			while (finishSudoku == false){	//get random for 0 to N
				current = rd.nextInt(size);
				if (Num[current] == false){
					Num[current] = true;
					Sudoku[numCount] = current;
					numCount++;
				}
				if (numCount == size)
					finishSudoku = true;
			}
				
			//System.out.println(numCount);
			
			//System.out.println(edge);
			/*for(int i=0;i<3;i++){
				for(int j=0; j<3; j++){
					sudokuMap[i][j] = Sudoku[3*i+j];  //get random for 0 or 8
				}
			}*/
			
			for(int i=0;i<=MAXVAL;i+=edge){
				for(int j=0; j<=MAXVAL; j+=edge){
					if((MAXVAL - i >= edge - 1) && (MAXVAL - j >= edge - 1)){
						for(int row=0; row<edge; row++){
							for(int column=0; column<edge; column++){
								sudokuMap[i+row][j+column] = Sudoku[row*edge+column];
							}
						}
						//check++;
					}
					else if((MAXVAL - i < edge - 1) && (MAXVAL - j < edge - 1)){
						for(int row=0; row<=MAXVAL - i; row++){
							for(int column=0; column<=MAXVAL - j; column++){
								sudokuMap[i+row][j+column] = Sudoku[row*edge+column];
							}
						}
						//check++;
					}
					else if(MAXVAL - i < edge - 1){
						for(int row=0; row<=MAXVAL - i; row++){
							for(int column=0; column<edge; column++){
								//System.out.println((i+row)+" "+(j+column)+" "+(row*edge+column));
								sudokuMap[i+row][j+column] = Sudoku[row*edge+column];
							}
						}
						//check++;
					}
					else if(MAXVAL - j < edge - 1){
						for(int row=0; row<edge; row++){
							for(int column=0; column<=MAXVAL - j; column++){
								//System.out.println((i+row)+" "+(j+column)+" "+(row*edge+column));
								sudokuMap[i+row][j+column] = Sudoku[row*edge+column];
							}
						}
						//check++;
					}
					else{
						System.out.println("Special Case!!");
					}
					
				}
			}
			//System.out.println(check);
			
	}
	
	public void hideInfo(){	//�äJ��T
		
		int k = 0;			//���K�Ʀr�}�C������
		int Rx = 0, Cx = 0;
		int Ri = 0, Ci = 0;
		int bound = L/2;
		int row = 0, column = 0;
		
		
		if(L%2 == 1){	//size is odd
			for(int i=0; i<imgHeight; i++){
				for(int j=0; j<imgWidth; j+=2){
					row = imgArray[i][j];
					column = imgArray[i][j+1];
					
					if(k >= digitNum){
						stegoArray[i][j] = row;
						stegoArray[i][j+1] = column; 
					}
					
					else if(row<=bound && column<=bound){	//Matrix���W�誺��t
						Rx = L; Cx = L;
						Ri = 0; Ci = 0;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					else if((255 - row)<=bound && (255 - column)<=bound){	//Matrix�k�U�誺��t
						Rx = 256; Cx = 256;
						Ri = 256 - L; Ci = 256 - L;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					
					else if((255 - column)<=bound && row<=bound){	//Matrix�k�W�誺��t
						Rx = L; Cx = 256;
						Ri = 0; Ci = 256 - L;
						
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					else if((255 - row)<=bound && column<=bound){	//Matrix���U�誺��t
						Rx = 256; Cx = L;
						Ri = 256 - L; Ci = 0;
						
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					
					//***********************************************************************
					else if(row<=bound && !(column<=bound) && !((255 - column)<=bound)){	//Matrix�W�誺��t
						Rx = L; Cx = column+bound+1;
						Ri = 0; Ci = column-bound;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;
									break;
								}
							}
						}
					}
					else if(column<=bound && !(row<=bound) && !((255 - row)<=bound)){	//Matrix���誺��t
						Rx = row+bound+1; Cx = L;
						Ri = row-bound; Ci = 0;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}					
					else if((255 - row)<=bound && !((255 - column)<=bound) && !(column<=bound)){ //�U�誺��t
						Rx = 256; Cx = column+bound+1;
						Ri = 256 - L; Ci = column-bound;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column; 
									break;
								}
							}
						}
					}
					else if((255 - column)<=bound && !((255 - row)<=bound) && !(row<=bound)){	//�k�誺��t
						Rx = row+bound+1; Cx = 256;
						Ri = row-bound; Ci = 256 - L;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					
					//***********************************************************************					
					else{			//�������϶�
						Rx = row+bound+1; Cx = column+bound+1;
						Ri = row-bound; Ci = column-bound;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;
									break;
								}
							}
						}
					}
					k++;
				}
			}
		}
		else{			//size is even
			for(int i=0; i<imgHeight; i++){
				for(int j=0; j<imgWidth; j+=2){
					row = imgArray[i][j];
					column = imgArray[i][j+1];
					
					if(k >= digitNum){
						stegoArray[i][j] = row;
						stegoArray[i][j+1] = column; 
					}
					else if(row<bound && column<bound){	//Matrix���W�誺��t
						Rx = L; Cx = L;
						Ri = 0; Ci = 0;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					else if((255 - row)<=bound && (255 - column)<=bound){	//Matrix�k�U�誺��t
						Rx = 256; Cx = 256;
						Ri = 256 - L; Ci = 256 - L;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					else if((255 - column)<=bound && row<bound){	//Matrix�k�W�誺��t
						Rx = L; Cx = 256;
						Ri = 0; Ci = 256 - L;
						
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					else if((255 - row)<=bound && column<bound){	//Matrix���U�誺��t
						Rx = 256; Cx = L;
						Ri = 256 - L; Ci = 0;
						
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					
					//***********************************************************************
					else if(row<bound && !(column < bound) && !((255 - column)<=bound)){	//Matrix�W�誺��t
						Rx = L; Cx = column+bound+1;
						Ri = 0; Ci = column-bound+1;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column; 
									break;
								}
							}
						}
					}
					else if(column<bound && !(row < bound) && !((255 - row)<=bound)){	//Matrix���誺��t
						Rx = row+bound+1; Cx = L;
						Ri = row-bound+1; Ci = 0;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					
					
					else if((255 - row)<=bound && !((255 - column)<=bound) && !(column<bound)){	//Matrix�U�誺��t
						Rx = 256; Cx = column+bound+1;
						Ri = 256 - L; Ci = column-bound+1;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column; 
									break;
								}
							}
						}
					}
					else if((255 - column)<=bound && !((255 - row)<=bound) && !(row<bound)){	//Matrix�k�誺��t
						Rx = row+bound+1; Cx = 256;
						Ri = row-bound+1; Ci = 256 - L;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					
					//***********************************************************************
					else{		//�������϶�
						Rx = row+bound+1; Cx = column+bound+1;
						Ri = row-bound+1; Ci = column-bound+1;
						for(row=Ri; row<Rx; row++){
							for(column=Ci; column<Cx; column++){
								if(k >= digitNum)System.out.println("k ="+ k);
								
								if(sudokuMap[row][column] == digitArray[k]){
									stegoArray[i][j] = row;
									stegoArray[i][j+1] = column;  
									break;
								}
							}
						}
					}
					k++;
				}
			}
		}
		
		
		//System.out.println("�w�äJ"+k+"��"+L*L+"�i��Ʀr!");
	}
		
	public void diffCompare(){	//Stego image �P Original image ���t�����
		double diff = 0;
		double psnr = 0, mse = 0;
		
		for(int i=1; i<=imgHeight; i++){
			for(int j=1; j<=imgWidth; j++){
				diff += (int)Math.pow(stegoArray[i][j] - imgArray[i][j], 2);
			}
		}
		mse = diff / (imgHeight*imgWidth);
		
		psnr=10*(Math.log10(Math.pow(255,2)/mse));
		
		System.out.println("SE = "+diff);
		System.out.println("MSE = "+mse);
		System.out.println("PSNR = "+psnr+" dB");	
	}
	
	public void extractInfo(String fn) throws IOException{	//���X��T
			int k = 0;
			int bitsCount = 0;
		
			bitObj = new BitIO(imgHeight*imgWidth);
			
		
			extractArray = new int[digitNum];
			int row = 0, column = 0;
		
			for(int i=0; i<imgHeight; i++){
				for(int j=0; j<imgWidth; j+=2){
					if(k >= digitNum){
						break;
					}
					
					row = stegoArray[i][j];
					column = stegoArray[i][j+1];
					
					extractArray[k++] = sudokuMap[row][column];
					if(sudokuMap[row][column] >= Math.pow(2, bitLength)){
						bitObj.appendToBitStream(sudokuMap[row][column], bitLength+1);
						bitsCount += bitLength+1;
					}
					else{
						bitObj.appendToBitStream(sudokuMap[row][column], bitLength);
						bitsCount += bitLength;
					}
				}
			}
			bitObj.outFile(fn);
			System.out.println("���X��T: "+bitsCount+" bits");
	}
	
	public void compareTwoFiles(String originalInfo, String extractInfo){	//����äJ�P���X����T
		BitIO bitObj_1 =  new BitIO(imgHeight*imgWidth);
		BitIO bitObj_2 =  new BitIO(imgHeight*imgWidth);
		
		
		int result = 0;
		
		bitObj_1.inFile(originalInfo);
		bitObj_2.inFile(extractInfo);
		
		for(int i = 0; i < digitNum; i++){
			result += Math.pow((bitObj_1.extractFromBitStream(3) - bitObj_2.extractFromBitStream(3)), 2);	
		}
		
		if(result == 0){ 
			System.out.println("Hidden data and extracted data are identical!");
		}
		else{ 
			System.out.println("Hidden data and extracted data are different!");
			System.out.println("���Ʈt��: "+result);
		}
	}
	
	public void printInfo(int size){
		
		System.out.print("���K��T: ");
		for(int i = 0; i < 20; i++){
			System.out.print(infoArray[i]);
		}
		
		System.out.println();
		
		System.out.print("���K�Ʀr: ");
		for(int i = 0; i < size; i++){
			System.out.print(digitArray[i]+" ");
		}
		
		System.out.println();
	}
	
	public void printMatrix(int size){
		
		int height = (int)Math.sqrt(size);
		int width = (int)Math.sqrt(size);
		
		
		for(int i=0;i<height;i++){
			for(int j=0; j<width; j++){
				System.out.print(sudokuMap[i][j]+" ");
			}
			System.out.print("\n");
		}
	}
	
}
