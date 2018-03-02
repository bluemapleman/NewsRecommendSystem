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
class MapValueComparator implements Comparator<Map.Entry<Long, Double>> {

	@Override
	public int compare(Entry<Long, Double> me1, Entry<Long, Double> me2) {

		return me1.getValue().compareTo(me2.getValue());
	}
}