import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Program {
	
	Map<Long, OSMNode> osmData = new HashMap<Long, OSMNode>();
	
	Set<Long> open = new HashSet<Long>();
	
	Set<Long> closed = new HashSet<Long>();
	
	Map<Long, Double> G = new TreeMap<Long, Double>();
	//Map<Long, Double> F = new TreeMap<Long, Double>();
	DoubleMap F = new DoubleMap();
	Map<Long, Long> route = new HashMap<Long, Long>();
	
	public static void main(String[] args) {
		Program pr = new Program();
		pr.run();
	}
	
	void run() {
		long start = 2192749506L; long end = 5775925937L;//spb points
		String fileName = "spb.xml";
		
		loadOSMMap(fileName);
		System.out.println("loaded");
		
		boolean success = aStarFind(start, end);
		System.out.println(success?"Success":"Route not found");
		if(success)
			printRouteReverse(start, end);
		
	}
	
	boolean  aStarFind(long start, long end) {
		open.add(start);
		G.put(start, 0.0);
		OSMNode startNode = osmData.get(start);
		OSMNode endNode = osmData.get(end);
		F.put(start, G.get(start) + h(startNode.lon, startNode.lat, endNode.lon, endNode.lat));
		while(!open.isEmpty()) {
			long curr = getFromOpenMinF();
			if(curr == end)return true;
			open.remove(curr);
			F.remove(curr);
			closed.add(curr);
			OSMNode currNode = osmData.get(curr);
			//цикл перебора соседей curr, не входящих в close
			Iterator<OSMNode> iter = currNode.linkedTo.iterator();
			while(iter.hasNext()) {
				OSMNode neighborNode = iter.next();
				if(closed.contains(neighborNode.id))continue;
				long neighbor = neighborNode.id;
				double tempG = G.get(curr) + dist(currNode.lon, currNode.lat, neighborNode.lon, neighborNode.lat);
				if(!open.contains(neighbor) || tempG < G.get(neighbor)) {
					route.put(neighbor, curr);
					G.put(neighbor, tempG);
					F.put(neighbor, tempG + h(neighborNode.lon, neighborNode.lat, endNode.lon, endNode.lat));
				}
				if(!open.contains(neighbor))
					open.add(neighbor);
				
				
			}
			
			
		}
		return false;
	}
	
	Long getFromOpenMinF() {
		return F.getIdOfMinValue();
	}
	/*Long getFromOpenMinF(){
		if(open.isEmpty())return null;
		Iterator<Long> iter = open.iterator();
		iter.hasNext();
		Long minId = iter.next();
		Double minValue = F.get(minId);
		
		while(iter.hasNext()) {
			Long id = iter.next();
			Double val = F.get(id);
			if(val < minValue) {
				minValue = val;
				minId = id;
			}
		}
		return minId;
	}*/
	
	double h(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	void loadOSMMap(String filePath) {
		double progressScale = 0.05;
		double progressNextMark = progressScale;
		
		File file = new File(filePath);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
		        .newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			System.out.println("parsing xml file...");
			Document document = documentBuilder.parse(file);
			System.out.println("xml file parsed.");
			System.out.println("getting node list from document...");
			NodeList osmNodeList = document.getElementsByTagName("node");
			System.out.println("node list from document was get.");
			
			
			int nodeListLength = osmNodeList.getLength();
			for(int i = 0; i < nodeListLength; i++) {//цикл иду по всем объектам node, добавляю каждый в osmData(id, координаты)
				NamedNodeMap attrs = osmNodeList.item(i).getAttributes();
				double lat = Double.parseDouble(attrs.getNamedItem("lat").getTextContent());
				double lon = Double.parseDouble(attrs.getNamedItem("lon").getTextContent());
				long id = Long.parseLong(attrs.getNamedItem("id").getTextContent());
				
				//System.out.println("id: "+ id + ", latitude: " + lat + ", longitude: " + lon);
				OSMNode osmNode = new OSMNode();
				osmNode.id = id;
				osmNode.lat = lat;
				osmNode.lon = lon;
				osmData.put(id, osmNode);
				
				double currLoadProgress = 1.0*(i+1)/nodeListLength;
				if(currLoadProgress >= progressNextMark) {
					System.out.println((i+1)+" of "+ nodeListLength+ " nodes (" + Math.round(currLoadProgress*100) + "%)");
					progressNextMark += progressScale;
				}
			}
			
			NodeList osmWayList = document.getElementsByTagName("way");
			int wayListLength = osmWayList.getLength();
			for(int i = 0; i < wayListLength; i++) {//цикл по всем объектам way
				Node way = osmWayList.item(i);
				Long[] nodeIds = getIdsFromWayNode(way);
				for(int j = 0; j < nodeIds.length; j++) {
					OSMNode currNode = osmData.get(nodeIds[j]);
					if(currNode == null) {
						System.out.println("node id="+ nodeIds[j]+" wasn't in Nodes");
						continue;
					}
					if(j > 0) {
						OSMNode prevNode = osmData.get(nodeIds[j-1]);
						currNode.linkedTo.add(prevNode);
					}
					if(j < (nodeIds.length-1)) {
						OSMNode nextNode = osmData.get(nodeIds[j+1]);
						currNode.linkedTo.add(nextNode);
					}
					
					
				}
			}
			
		}
		catch(Exception ex) {
			System.out.println(ex.toString() + ": " + ex.getMessage());
		}
		
		
	}
	
	Long[] getIdsFromWayNode(Node way) {
		ArrayList<Long> list = new ArrayList<Long>();
		NodeList wayChildNodes = way.getChildNodes();
		int wayChildListLength = wayChildNodes.getLength();
		for(int j = 0; j < wayChildListLength; j++) {
			Node currItem = wayChildNodes.item(j);
			if(currItem.getNodeType() != Node.ELEMENT_NODE)continue;
			if(!currItem.getNodeName().equals("nd"))continue;
			String ref = currItem.getAttributes().getNamedItem("ref").getTextContent();//TODO добавить проверку getNamedItem("ref") на null
			long nodeId = Long.parseLong(ref);
			list.add(nodeId);
			
		}
		return list.toArray(new Long[0]);
	}
	void printRouteReverse(long start, long end) {
		long curr = end;
		while(curr!= start) {
			printPoint(curr);
			curr = route.get(curr);
		}
		printPoint(curr);
	}
	
	void printPoint(long id) {
		//printKibanaConsoleCommand(id);
		//System.out.println(id);
		printJsonData(id);
	}
	
	void printJsonData(long id) {
		OSMNode node = osmData.get(id);
		System.out.println("{");
		System.out.println("  \"id\": "+ "\"" + id + "\"," );
		System.out.println("  \"text\": \"Geo-point as an object "+ id +"\",");
		System.out.println("  \"location\": { ");
		System.out.println("    \"lat\": "+ node.lat + ",");
		System.out.println("    \"lon\": "+ node.lon );
		System.out.println("  }");
		//System.out.println("");
		System.out.println("},");
	}
	
	void printKibanaConsoleCommand(long id) {
		OSMNode node = osmData.get(id);
		System.out.println("PUT geo10/_doc/"+id);
		System.out.println("{");
		System.out.println("  \"text\": \"Geo-point as an object "+ id +"\",");
		System.out.println("  \"location\": { ");
		System.out.println("    \"lat\": "+ node.lat + ",");
		System.out.println("    \"lon\": "+ node.lon );
		System.out.println("  }");
		//System.out.println("");
		System.out.println("}");
	}
}
