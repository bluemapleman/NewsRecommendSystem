/**
 * 
 */
package top.qianxinyao.contentbasedrecommend;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年12月2日
 */
class MapValueComparator implements Comparator<Map.Entry<String, Double>> {

	@Override
	public int compare(Entry<String, Double> me1, Entry<String, Double> me2) {

		return me1.getValue().compareTo(me2.getValue());
	}
}