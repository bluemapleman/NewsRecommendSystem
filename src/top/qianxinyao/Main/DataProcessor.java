///**
// * 
// */
//package top.qianxinyao.Main;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.text.DecimalFormat;
//import java.util.HashMap;
//import java.util.Iterator;
//
//import org.apache.log4j.Logger;
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//
//
///**
// * @author qianxinyao
// * @email tomqianmaple@gmail.com
// * @github https://github.com/bluemapleman
// * @date 2016年10月16日
// */
//
//public class DataProcessor
//{
//	
//	private static Logger logger = Logger.getLogger(DataProcessor.class);
//	
//	private static HashMap<String,HashMap<String,Integer>> userLikes;
//	
//	/**
//	 * 读取数据表，获得用户的喜好数据
//	 */
//	public static void readDataTable(){
//		
//	}
//	
//	/**
//	 * 读取excel文件，获得用户的喜好数据
//	 * 
//	 * @param file
//	 */
//
//	public static void readExcelData(File file)
//	{
//		userLikes=new HashMap<String,HashMap<String,Integer>>();
//		
//		HSSFWorkbook wb=null;
//		
//		try
//		{
//			wb = new HSSFWorkbook(new FileInputStream(file));
//
//			HSSFSheet sheet = wb.getSheetAt(0);
//
//			// Iterate over each row in the sheet
//
//			Iterator<Row> rows = sheet.rowIterator();
//			
//			while (rows.hasNext())
//			{
//				HashMap<String,Integer> likes=new HashMap<String,Integer>();
//				
//				HSSFRow row = (HSSFRow) rows.next();
//				
//				// Iterate over each cell in the row and print out the cell"s
//
//				// content
//
//				Iterator<Cell> cells = row.cellIterator();
//				
//				
//				while (cells.hasNext())
//				{
//					Cell cell=cells.next();
//					
//					String cellValue=getCellValue((HSSFCell) cell);
//					
//					
//					logger.info(cellValue);	
//				    
//				}
//			}
//		}
//		catch (FileNotFoundException fe)
//		{
//			logger.error("Exception:" + fe.toString());
//		}
//		catch (Exception e)
//		{
//			logger.error("Exception:" + e.toString());
//		}
//		finally
//		{
//			if(null!=wb){
//				try
//				{
//					wb.close();
//				}
//				catch (IOException e)
//				{
//					// TODO Auto-generated catch block
//					logger.error("XSSFWorkbook close failed!");
//				}
//			}
//			logger.info("程序出现异常！请检查！");
//		}
//	}
//	
//
//	/**
//	 * 返回各种类型单元格值的字符串形式的方法
//	 * @param cell
//	 * @return
//	 */
//    private static String getCellValue(HSSFCell cell) {  
//            String cellValue = "";  
//            DecimalFormat df = new DecimalFormat("#");  
//            switch (cell.getCellType()) {  
//            case HSSFCell.CELL_TYPE_STRING:  
//                cellValue = cell.getRichStringCellValue().getString().trim();  
//                break;  
//            case HSSFCell.CELL_TYPE_NUMERIC:  
//                cellValue = df.format(cell.getNumericCellValue()).toString();  
//                break;  
//            case HSSFCell.CELL_TYPE_BOOLEAN:  
//                cellValue = String.valueOf(cell.getBooleanCellValue()).trim();  
//                break;  
//            case HSSFCell.CELL_TYPE_FORMULA:  
//                cellValue = cell.getCellFormula();  
//                break;  
//            default:  
//                cellValue = "";  
//            }  
//            return cellValue;  
//        }  
//
//
//	
//	
//	
//	/**
//	 * 分隔用户所喜好新闻数据的方法，获得用户喜好的所有新闻信息。
//	 * @param likes 
//	 * @param seperator 新闻数据分隔符
//	 * @return
//	 */
////	private static String[] seperateLikes(String likes,String seperator){
////		return likes.split(seperator); 
////	}
//
//}
