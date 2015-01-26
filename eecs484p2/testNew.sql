" select C.cT, A.user_id, A.first_name, A.last_name, A.year_of_birth, B.user_id, B.first_name, "+
" B.last_name, B.year_of_birth from "+
"  "+userTableName+"  A,  "+userTableName+"  B, "+
" ( "+
" 	select count(*) as cT, idf, idm from "+
" 	( "+
" 		select A.user_id as idf, B.user_id as idm, P.photo_id "+
" 		from  "+userTableName+"  A,  "+userTableName+"  B,  "+photoTableName+"  P,  "+tagTableName+"  T1,  "+tagTableName+"  T2 "+
" 		where "+
" 			(A.gender='female' and B.gender='male') "+
" 		and "+
" 			(A.year_of_birth-B.year_of_birth<4 and A.year_of_birth-B.year_of_birth>(-4)) "+
" 		and "+
" 			(A.user_id, B.user_id) not in "+
" 				(select user1_id, user2_id "+
" 				from  "+friendsTableName+"  "+
" 				union "+
" 				select user2_id, user1_id "+
" 				from  "+friendsTableName+" ) "+
" 		and "+
" 			(T1.tag_subject_id=A.user_id and T2.tag_subject_id=B.user_id) "+
" 			and "+
" 			(P.photo_id=T1.tag_photo_id and P.photo_id=T2.tag_photo_id) "+
" 	) "+
" 	group by (idf, idm) "+
" 	having count(*)>0 "+
" )C "+
" where A.user_id=C.idf and B.user_id=C.idm "+
" order by 1 desc, 2 asc, 6 asc "

