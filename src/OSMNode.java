import java.util.LinkedList;
import java.util.List;

public class OSMNode {
	public long id;
	public double lat;
	public double lon;
	//� ������ linkedTo ������� id(Long) ��� ������ �� ������� OSMNode?
	//public List<Long> linkedTo = new LinkedList<Long>();
	public List<OSMNode> linkedTo = new LinkedList<OSMNode>();
}
