import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class DoubleMap {
	Map<Long, Double> map1;
	TreeMap<Double, LinkedList<Long>> map2;
	public DoubleMap() {
		map1 = new HashMap<Long, Double>();
		map2 = new TreeMap<Double, LinkedList<Long>>();
	}
	public void put(Long key, Double value) {
		if(key == null || value == null)throw new IllegalArgumentException();
		
		Double prevValue = map1.put(key, value);
		if(prevValue == null) {//в map1 не было такого key, т.е. он только сейчас добавился. Поэтому нужно просто добавить пару в map2
			addToMap2(key, value);
		}
		else {//нужно "мигрировать" теперь id к другому dvalue
			changeDValueOfId(key, prevValue, value);
		}
	}
	
	void addToMap2(Long id, Double value) {
		LinkedList<Long> idList = map2.get(value);
		if(idList == null) {
			idList = new LinkedList<Long>();
			map2.put(value, idList);
		}
		idList.add(id);
	}
	
	void changeDValueOfId(Long id, Double oldVal, Double newVal) {
		LinkedList<Long> idList = map2.get(oldVal);
		if(idList.size() == 1) {
			if(!idList.getFirst().equals(id) ) 
				throw new IllegalStateException(); 
			
			map2.remove(oldVal);
			addToMap2(id, newVal);
		}
		else {
			Iterator<Long> iter = idList.iterator();
			while(iter.hasNext()) {
				Long curId = iter.next();
				if(id.equals(curId) ) {
					iter.remove();
					addToMap2(id, newVal);
					return;
				}
			}
			throw new IllegalStateException();
				
		}
	}
	
	public void remove(Long id) {
		Double value = map1.remove(id);
		if(value == null) 
			return;
		
		LinkedList<Long> idList = map2.get(value);
		if(idList.size() == 1) {
			if(!idList.getFirst().equals(id) )
				throw new IllegalStateException(); 
			
			map2.remove(value);
		}
		else {
			Iterator<Long> iter = idList.iterator();
			while(iter.hasNext()) {
				Long curId = iter.next();
				if(id.equals(curId) ) {
					iter.remove();
					return;
				}
			}
			throw new IllegalStateException();
				
		}
	}
	
	public Long getIdOfMinValue() {
		if(map1.isEmpty())return null;
		Entry<Double, LinkedList<Long>> firstEntry = map2.firstEntry();
		return firstEntry.getValue().getFirst();
	}
	
	public boolean isEmpty() {
		return map1.isEmpty();
	}
}
