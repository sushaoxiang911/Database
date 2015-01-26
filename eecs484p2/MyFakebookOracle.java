package project2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;
//	Yao Xiao 		xyaoinum 	33249434
//	Shaoxiang Su 	ssx			36877438
public class MyFakebookOracle extends FakebookOracle {
	
	static String prefix = "singhmk.";
	
	// You must use the following variable as the JDBC connection
	Connection oracleConnection = null;
	
	// You must refer to the following variables for the corresponding tables in your database
	String cityTableName = null;
	String userTableName = null;
	String friendsTableName = null;
	String currentCityTableName = null;
	String hometownCityTableName = null;
	String programTableName = null;
	String educationTableName = null;
	String eventTableName = null;
	String participantTableName = null;
	String albumTableName = null;
	String photoTableName = null;
	String coverPhotoTableName = null;
	String tagTableName = null;
	
	
	// DO NOT modify this constructor
	public MyFakebookOracle(String u, Connection c) {
		super();
		String dataType = u;
		oracleConnection = c;
		// You will use the following tables in your Java code
		cityTableName = prefix+dataType+"_CITIES";
		userTableName = prefix+dataType+"_USERS";
		friendsTableName = prefix+dataType+"_FRIENDS";
		currentCityTableName = prefix+dataType+"_USER_CURRENT_CITY";
		hometownCityTableName = prefix+dataType+"_USER_HOMETOWN_CITY";
		programTableName = prefix+dataType+"_PROGRAMS";
		educationTableName = prefix+dataType+"_EDUCATION";
		eventTableName = prefix+dataType+"_USER_EVENTS";
		albumTableName = prefix+dataType+"_ALBUMS";
		photoTableName = prefix+dataType+"_PHOTOS";
		tagTableName = prefix+dataType+"_TAGS";
	}
	
	
	@Override
	// ***** Query 0 *****
	// This query is given to your for free;
	// You can use it as an example to help you write your own code
	//
	public void findMonthOfBirthInfo() throws SQLException{ 
		
		// Scrollable result set allows us to read forward (using next())
		// and also backward.  
		// This is needed here to support the user of isFirst() and isLast() methods,
		// but in many cases you will not need it.
		// To create a "normal" (unscrollable) statement, you would simply call
		// Statement stmt = oracleConnection.createStatement();
		//
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		        ResultSet.CONCUR_READ_ONLY);
		
		// For each month, find the number of friends born that month
		// Sort them in descending order of count
		ResultSet rst = stmt.executeQuery("select count(*), month_of_birth from "+
				userTableName+
				" where month_of_birth is not null group by month_of_birth order by 1 desc");
		
		this.monthOfMostFriend = 0;
		this.monthOfLeastFriend = 0;
		this.totalFriendsWithMonthOfBirth = 0;
		
		// Get the month with most friends, and the month with least friends.
		// (Notice that this only considers months for which the number of friends is > 0)
		// Also, count how many total friends have listed month of birth (i.e., month_of_birth not null)
		//
		while(rst.next()) {
			int count = rst.getInt(1);
			int month = rst.getInt(2);
			if (rst.isFirst())
				this.monthOfMostFriend = month;
			if (rst.isLast())
				this.monthOfLeastFriend = month;
			this.totalFriendsWithMonthOfBirth += count;
		}
		
		// Get the names of friends born in the "most" month
		rst = stmt.executeQuery("select user_id, first_name, last_name from "+
				userTableName+" where month_of_birth="+this.monthOfMostFriend);
		while(rst.next()) {
			Long uid = rst.getLong(1);
			String firstName = rst.getString(2);
			String lastName = rst.getString(3);
			this.friendsInMonthOfMost.add(new UserInfo(uid, firstName, lastName));
		}
		
		// Get the names of friends born in the "least" month
		rst = stmt.executeQuery("select first_name, last_name, user_id from "+
				userTableName+" where month_of_birth="+this.monthOfLeastFriend);
		while(rst.next()){
			String firstName = rst.getString(1);
			String lastName = rst.getString(2);
			Long uid = rst.getLong(3);
			this.friendsInMonthOfLeast.add(new UserInfo(uid, firstName, lastName));
		}
		
		// Close statement and result set
		rst.close();
		stmt.close();
	}

	
	@Override
	// ***** Query 1 *****
	// Find information about friend names:
	// (1) The longest last name (if there is a tie, include all in result)
	// (2) The shortest last name (if there is a tie, include all in result)
	// (3) The most common last name, and the number of times it appears (if there is a tie, include all in result)
	//
	public void findNameInfo() throws SQLException { 
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		        ResultSet.CONCUR_READ_ONLY);
		
		int MinLength = Integer.MAX_VALUE;
		int MaxLength = 0;
		
		ResultSet rst = stmt.executeQuery("select distinct last_name from "+
				userTableName);
		
		while (rst.next())
		{
			int TempLength = rst.getString(1).length();
			if (TempLength < MinLength)
			{
				MinLength = TempLength;
				this.shortestLastNames.clear();
				this.shortestLastNames.add(rst.getString(1));
			}
			if (TempLength == MinLength)
			{
				this.shortestLastNames.add(rst.getString(1));
			}
			if (TempLength > MaxLength)
			{
				MaxLength = TempLength;
				this.longestLastNames.clear();
				this.longestLastNames.add(rst.getString(1));
			}
			if (TempLength == MaxLength)
			{
				this.longestLastNames.add(rst.getString(1));
			}
		}
		
		rst = stmt.executeQuery(
				"select count(*), last_name"+
				" from "+userTableName+
				" where last_name is not null group by last_name" +
				" order by 1 desc ");
		rst.next();
		int Common = rst.getInt(1);
		this.mostCommonLastNamesCount = Common;
		this.mostCommonLastNames.add(rst.getString(2));
		while (rst.next())
		{
			if (rst.getInt(1) < Common)
			{
				break;
			}
			else
			{
				this.mostCommonLastNames.add(rst.getString(2));
			}
		}
		
	}
	
	@Override
	// ***** Query 2 *****
	// Find the user(s) who have no friends in the network
	//
	// Be careful on this query!
	// Remember that if two users are friends, the friends table
	// only contains the pair of user ids once, subject to 
	// the constraint that user1_id < user2_id
	//
	public void lonelyFriends() throws SQLException {
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
		ResultSet.CONCUR_READ_ONLY);
		this.countLonelyFriends = 0;
		ResultSet rst = stmt.executeQuery("select user_id, first_name, last_name from "+userTableName+
				" where user_id not in (select distinct user1_id from "+
				friendsTableName+" union select distinct user2_id from "+friendsTableName+" )");
		while(rst.next()) {
			this.lonelyFriends.add(new UserInfo(rst.getLong(1),rst.getString(2),rst.getString(3)));
			this.countLonelyFriends++;
		}
	}
	 

	@Override
	// ***** Query 3 *****
	// Find the users who still live in their hometowns
	// (I.e., current_city = hometown_city)
	//
	
	public void liveAtHome() throws SQLException {
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
				ResultSet.CONCUR_READ_ONLY);
		this.countLiveAtHome = 0;
		ResultSet rst = stmt.executeQuery("select U.user_id, U.first_name, U.last_name from "+currentCityTableName+
				" C, "+hometownCityTableName+" H, "+userTableName+
				" U where U.user_id=C.user_id and U.user_id=H.user_id and C. CURRENT_CITY_ID=H.HOMETOWN_CITY_ID");
		while(rst.next()) {
			this.liveAtHome.add(new UserInfo(rst.getLong(1),rst.getString(2),rst.getString(3)));
			this.countLiveAtHome++;
		}
		
	}



	@Override
	// **** Query 4 ****
	// Find the top-n photos based on the number of tagged users
	// If there are ties, choose the photo with the smaller numeric PhotoID first
	// 
	public void findPhotosWithMostTags(int n) throws SQLException { 
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		        ResultSet.CONCUR_READ_ONLY);
		ResultSet rst = stmt.executeQuery(
				" select count(*), tag_photo_id from "+tagTableName+
				" group by tag_photo_id " +//tag_subject_id is user_id????
				" order by 1 desc,2 asc");
		
		int i = 0;
		while (rst.next())
		{
			if (i == n)
			{
				break;
			}
			String photoId = rst.getString(2);
			Statement stmt1 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			        ResultSet.CONCUR_READ_ONLY);
			
			ResultSet PhotoQuery = stmt1.executeQuery(
					"select A.album_id, A.album_name, P.photo_caption,P.photo_link "+
					" from "+albumTableName+" A,"+photoTableName+" P "+
					" where P.album_id = A.album_id AND P.photo_id ="+photoId);
			PhotoQuery.next();
			String albumId = PhotoQuery.getString(1);
			String albumName = PhotoQuery.getString(2);
			String photoCaption = PhotoQuery.getString(3);
			String photoLink = PhotoQuery.getString(4);
			TaggedPhotoInfo tp = new TaggedPhotoInfo(new PhotoInfo(photoId, albumId, albumName, photoCaption, photoLink));
			
			
			Statement stmt2 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			        ResultSet.CONCUR_READ_ONLY);
			
			
			ResultSet CorresUser = stmt2.executeQuery(
					" select U.user_id, U.first_name, U.last_name " +
					" from "+tagTableName+" T,"+userTableName+" U" +
					" where T.tag_subject_id=U.user_id AND T.tag_photo_id="+photoId
					);
			while (CorresUser.next())
			{
				tp.addTaggedUser(new UserInfo(CorresUser.getLong(1),CorresUser.getString(2),
							CorresUser.getString(3)));
			}
			this.photosWithMostTags.add(tp);
			i++;
		}
	}

	
	
	
	@Override
	// **** Query 5 ****
	// Find suggested "match pairs" of friends, using the following criteria:
	// (1) One of the friends is female, and the other is male
	// (2) Their age difference is within "yearDiff"
	// (3) They are not friends with one another
	// (4) They should be tagged together in at least one photo
	//
	// You should up to n "match pairs"
	// If there are more than n match pairs, you should break ties as follows:
	// (i) First choose the pairs with the largest number of shared photos
	// (ii) If there are still ties, choose the pair with the smaller user_id for the female
	// (iii) If there are still ties, choose the pair with the smaller user_id for the male
	//
	public void matchMaker(int n, int yearDiff) throws SQLException {
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		        ResultSet.CONCUR_READ_ONLY);
		ResultSet rst = stmt.executeQuery(
				"select M.cT, GG.user_id, GG.first_name, GG.last_name, GG.year_of_birth, "+
				"BB.user_id, BB.first_name, BB.last_name, BB.year_of_birth "+
				" from "+userTableName+" GG, "+userTableName+" BB, "+
				" (select count(*) as cT,T.idf,T.idm"+
			" from ("+
				" select A.user_id as idf, B.user_id as idm"+
				" from "+userTableName+" A,"+userTableName+" B"+
				" where A.gender = 'female' AND B.gender = 'male'"+
				" intersect"+
				" select C.user_id as idf, D.user_id as idm"+
				" from "+userTableName+" C,"+userTableName+" D"+
				" where C.year_of_birth-D.year_of_birth>= "+(-yearDiff)+
				" AND C.year_of_birth-D.year_of_birth<= "+yearDiff+
				" intersect"+
				" select E.user_id as idf, F.user_id as idm"+
				" from "+userTableName+" E,"+userTableName+" F"+
				" where (E.user_id, F.user_id) not in"+
					" (select G.user1_id,G.user2_id"+
					" from "+friendsTableName+" G"+
					" union "+
					" select H.user2_id,H.user1_id"+
					" from "+friendsTableName+" H"+")"+
			" ) T,"+tagTableName+" S,"+tagTableName+" R"+
			" where T.idm=S.tag_subject_id AND T.idf=R.tag_subject_id AND S.tag_photo_id=R.tag_photo_id"+
			" group by (idf,idm)"+
			" having count(*)>0"+
			" ) M "+
			" where M.idf=GG.user_id and M.idm=BB.user_id" + 
			" order by 1 desc,2 asc,6 asc"
				);
		int temp=0;
		while(rst.next()&&temp<n)
		{
			temp++;
			MatchPair mp = new MatchPair(rst.getLong(2), rst.getString(3), rst.getString(4), 
					rst.getInt(5), rst.getLong(6), rst.getString(7), rst.getString(8), rst.getInt(9));
			
			
			Statement stmt0 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			        ResultSet.CONCUR_READ_ONLY);
		
			ResultSet rst0 = stmt0.executeQuery(
			" select TA1.tag_photo_id, AL.album_id, AL.album_name, PH.photo_caption, PH.photo_link"+
			" from "+tagTableName+" TA1, "+tagTableName+" TA2, "+albumTableName+" AL, "+photoTableName+
			" PH where TA1.tag_subject_id= "+mp.femaleId+" and TA2.tag_subject_id= "+mp.maleId+
			" and TA1.tag_photo_id=Ta2.tag_photo_id and PH.photo_id=TA1.tag_photo_id "+
			"and PH.album_id=AL.album_id ");
			while(rst0.next())
			{
				mp.addSharedPhoto(new PhotoInfo(rst0.getString(1), rst0.getString(2), 
						rst0.getString(3), rst0.getString(4), rst0.getString(5)));
			}
			rst0.close();
			this.bestMatches.add(mp);
			stmt0.close();
			
		}
	}

	
	
	// **** Query 6 ****
	// Suggest friends based on mutual friends
	// 
	// Find the top n pairs of users in the database who share the most
	// friends, but such that the two users are not friends themselves.
	//
	// Your output will consist of a set of pairs (user1_id, user2_id)
	// No pair should appear in the result twice; you should always order the pairs so that
	// user1_id < user2_id
	//
	// If there are ties, you should give priority to the pair with the smaller user1_id.
	// If there are still ties, give priority to the pair with the smaller user2_id.
	//
	@Override
	public void suggestFriendsByMutualFriends(int n) throws SQLException {
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		        ResultSet.CONCUR_READ_ONLY);
	
		ResultSet rst = stmt.executeQuery(
		" select R.cT, P.user_id,P.first_name,P.last_name,Q.user_id,Q.first_name,Q.last_name "+
			" from ( select count(*) as cT,E.u1 as uu1,E.u2 as uu2 "+
				" from ("+
				" select A.user_id as u1, B.user_id as u2 "+
				" from "+userTableName+" A,"+userTableName+" B,"+friendsTableName+" C,"+friendsTableName+" D"+
				" where (A.user_id,B.user_id) not in "+
					"(select user1_id,user2_id " +
					" from "+friendsTableName+" ) "+
				" AND A.user_id < B.user_id "+
				" AND A.user_id=C.user1_id AND B.user_id=D.user1_id AND C.user2_id=D.user2_id "+
				" UNION ALL "+
				" select A.user_id as u1, B.user_id as u2 "+
				" from "+userTableName+" A,"+userTableName+" B,"+friendsTableName+" C,"+friendsTableName+" D"+
				" where (A.user_id,B.user_id) not in "+
					"(select user1_id,user2_id " +
					" from "+friendsTableName+" ) "+
				" AND A.user_id < B.user_id "+
				" AND A.user_id=C.user2_id AND B.user_id=D.user1_id AND C.user1_id=D.user2_id "+
				" UNION ALL "+
				" select A.user_id as u1, B.user_id as u2 "+
				" from "+userTableName+" A,"+userTableName+" B,"+friendsTableName+" C,"+friendsTableName+" D"+
				" where (A.user_id,B.user_id) not in "+
					"(select user1_id,user2_id " +
					" from "+friendsTableName+" ) "+
				" AND A.user_id < B.user_id "+
				" AND A.user_id=C.user1_id AND B.user_id=D.user2_id AND C.user2_id=D.user1_id "+
				" UNION ALL "+
				" select A.user_id as u1, B.user_id as u2 "+
				" from "+userTableName+" A,"+userTableName+" B,"+friendsTableName+" C,"+friendsTableName+" D"+
				" where (A.user_id,B.user_id) not in "+
					"(select user1_id,user2_id " +
					" from "+friendsTableName+" ) "+
				" AND A.user_id < B.user_id "+
				" AND A.user_id=C.user2_id AND B.user_id=D.user2_id AND C.user1_id=D.user1_id "+
				" )E "+
				" group by (u1,u2)"+
			" ) R,"+userTableName+" P,"+userTableName+" Q "+
			" where R.uu1 = P.user_id AND R.uu2 = Q.user_id "+
			" order by 1 desc,2 asc,5 asc "
		);
		int i = 0;
		while (rst.next())
		{
			if (i == n)
			{
				break;
			}
			Long user1_id = rst.getLong(2);
			String user1FirstName = rst.getString(3);
			String user1LastName = rst.getString(4);
			Long user2_id = rst.getLong(5);
			String user2FirstName = rst.getString(6);
			String user2LastName = rst.getString(7);
			FriendsPair p = new FriendsPair(user1_id, user1FirstName, user1LastName, user2_id, user2FirstName, user2LastName);
			Statement stmt1 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			
			ResultSet rst1 = stmt1.executeQuery(
					"  select user_id, first_name, last_name from  "+userTableName+"  "+
							" where user_id in "+
							" ((select A.user1_id from  "+friendsTableName+"  A "+
							" where A.user2_id="+p.user1Id +
							" union "+
							" select A.user2_id from  "+friendsTableName+"  A "+
							" where A.user1_id="+p.user1Id +" )"+
							" intersect "+
							" (select A.user1_id from  "+friendsTableName+"  A "+
							" where A.user2_id="+p.user2Id +
							" union "+
							" select A.user2_id from  "+friendsTableName+"  A "+
							" where A.user1_id="+p.user2Id+" ))"
					);
			while(rst1.next())
			{
				p.addSharedFriend(rst1.getLong(1), rst1.getString(2), rst1.getString(3));
			}
			this.suggestedFriendsPairs.add(p);
			i++;
		}
	}
	
	
	//@Override
	// ***** Query 7 *****
	// Given the ID of a user, find information about that
	// user's oldest friend and youngest friend
	// 
	// If two users have exactly the same age, meaning that they were born
	// on the same day, then assume that the one with the larger user_id is older
	//
	public void findAgeInfo(Long user_id) throws SQLException {
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
				ResultSet.CONCUR_READ_ONLY);
		ResultSet rst = stmt.executeQuery("select user_id, first_name, last_name, YEAR_OF_BIRTH, MONTH_OF_BIRTH, DAY_OF_BIRTH from "+
				userTableName+" where user_id in (select distinct user1_id as id from "+friendsTableName+" where user2_id= "
				+user_id+" union select distinct user2_id as id from "+friendsTableName+" where user1_id= "
			+user_id+" ) order by YEAR_OF_BIRTH desc, MONTH_OF_BIRTH desc, DAY_OF_BIRTH desc, user_id desc");
			
		rst.next();
		this.youngestFriend=new UserInfo(rst.getLong(1),rst.getString(2),rst.getString(3));
		rst.last();
		this.oldestFriend=new UserInfo(rst.getLong(1),rst.getString(2),rst.getString(3));
		
		
	}
	
	
	@Override
	// ***** Query 8 *****
	// 
	// Find the name of the city with the most events, as well as the number of 
	// events in that city.  If there is a tie, return the names of all of the (tied) cities.
	//
	public void findEventCities() throws SQLException {
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
				ResultSet.CONCUR_READ_ONLY);
		ResultSet rst = stmt.executeQuery(
				" select count(*), EVENT_CITY_ID"+
				" from "+ eventTableName+
				" group by event_city_id" +
				" order by 1 desc"
				);
		
		rst.next();
		this.eventCount=rst.getInt(1);
		for(int i=0;i<this.eventCount;i++)
		{
			Statement stmt1 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rst0 = stmt1.executeQuery("select  CITY_NAME from "+cityTableName+" where CITY_ID="+
					rst.getLong(2));
			rst0.next();
			this.popularCityNames.add(rst0.getString(1));
		}
		
	}
	
	
	
	@Override
//	 ***** Query 9 *****
	//
	// Find pairs of potential siblings and print them out in the following format:
	//   # pairs of siblings
	//   sibling1 lastname(id) and sibling2 lastname(id)
	//   siblingA lastname(id) and siblingB lastname(id)  etc.
	//
	// A pair of users are potential siblings if they have the same last name and hometown, if they are friends, and
	// if they are less than 10 years apart in age.  Pairs of siblings are returned with the lower user_id user first
	// on the line.  They are ordered based on the first user_id and in the event of a tie, the second user_id.
	//  
	//
	public void findPotentialSiblings() throws SQLException {
		Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
				ResultSet.CONCUR_READ_ONLY);
		ResultSet rst = stmt.executeQuery(
				" select U1.user_id,U1.first_name,U1.last_name,U2.user_id,U2.first_name,U2.last_name "+
						" from "+userTableName+" U1, "+userTableName+" U2, "+
						" (select A.user_id as id1, B.user_id as id2 from  "+userTableName+"  A, "+
						userTableName+"  B,  "+hometownCityTableName+"  H1,  "+hometownCityTableName+"  H2 "+
						" where "+
						" A.user_id<B.user_id "+
						" and (A.user_id,B.user_id) in (select user1_id as id1, user2_id as id2 from "+friendsTableName+" ) "+
						" and A.year_of_birth-B.year_of_birth>-10 and A.year_of_birth-B.year_of_birth<10 "+
						" and A.last_name=B.last_name "+
						" and H1.user_id=A.user_id "+
						" and H2.user_id=B.user_id "+
						" and H1.hometown_city_id=H2.hometown_city_id) U3"+
						" where U1.user_id=U3.id1 and U2.user_id=U3.id2 "+
						" order by 1 asc, 4 asc"
				);	
		while(rst.next())
		{
			this.siblings.add(new SiblingInfo(rst.getLong(1), rst.getString(2), 
					rst.getString(3), rst.getLong(4), rst.getString(5), rst.getString(6)));
		}
		
		
	}
	
}