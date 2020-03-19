
package populate;

import java.util.LinkedList;
import java.util.List;

import com.wrox.expertj2ee.ticket.boxoffice.Seat;

public class Populator {
	
	private int[] allIds;
	
	public List populate(int rows, int cols) throws Exception {		
		List l = new LinkedList();
		int sid = 1;
		for (int row = 1; row <= rows; row++) {
			for (int col = 1; col <= cols; col++) {
				l.add(newSeat(sid++, row, col, cols));
			}
		}
		
		allIds = new int[l.size()];
		for (int i = 0; i < allIds.length; i++) {
			allIds[i] = ((Seat) l.get(i)).getId();
		}
		
		return l;
	}
	
	public int[] getAllIds() {
		return allIds;
	}
	
	
	private Seat newSeat(int sid, int row, int col, int cols) {
		char c = 'A' - 1;
		c += row;
		String name = c + "" + col;
		
		// 1-4
		int clazz = 1 + (row / 4);
		if (col < 2 || (cols - col < 2))
			++clazz;
			
		int l = 0, r = 0;
		if (col > 1)
			l = sid - 1;
		if (col < cols)
			r = sid + 1;
			
		Seat s = new Seat(sid, name, clazz, l, r);
		//System.out.println(s);
		return s;	
//		s.setId(new Integer(sid));
//		s.setName(name);
//		s.setRowOrBox(row);
//		s.setPositionInRow(col);
//		s.setClassId(clazz);
		
		
	}


	public static void main(String[] args) {
		try {
			Populator p = new Populator();
			List l = p.populate(2,6);
			String sql = p.generateSql(1, l);
			System.out.println(sql);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	 
	
	public String generateSql(int seatPlanId, List l) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < l.size(); i++) {
			Seat s = (Seat) l.get(i);
			String left = s.getLeft() == 0 ? "null" : "" + s.getLeft();
			String right = s.getRight() == 0 ? "null" : "" + s.getRight();
			String sql = "insert into seat(seat_id, name, left, right) values (" + s.getId() + ", '" + s.getName() + "', " + left + ", " + right + ")";
			
			sb.append(sql + ";\n");
			sql = "insert into seat_plan_seat(fk_seat_id, fk_seating_plan_id, fk_seat_class_id) values (" + s.getId() + ", " + seatPlanId + ", " + s.getSeatClassId() + ")";
			sb.append(sql + ";\n");
		}
		return sb.toString();
	}

}
