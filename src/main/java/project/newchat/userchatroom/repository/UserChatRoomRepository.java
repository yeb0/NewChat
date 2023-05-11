package project.newchat.userchatroom.repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import project.newchat.userchatroom.domain.UserChatRoom;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "1000")})
  Long countByChatRoomId(Long roomId); // 비관적 락

  @Query("select count(*) from UserChatRoom u where u.chatRoom.id = ?1 ")
  Long countNonLockByChatRoomId(Long roomId); // test 용도
}
