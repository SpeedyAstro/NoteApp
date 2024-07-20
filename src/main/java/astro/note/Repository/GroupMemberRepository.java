package astro.note.Repository;

import astro.note.entity.Group;
import astro.note.entity.GroupMember;
import astro.note.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember,Long> {
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user = :user AND gm.group = :group")
    GroupMember findByUserAndGroup(@Param("user") User user, @Param("group") Group group);

    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user = :user")
    List<Group> findGroupsByUser(User user);

    /*
    * This method is used to find all users in a group
    * Why JOIN FETCH?:
    * When you fetch a GroupMember, you often need the associated User as well.
    * Normally, JPA might load the User lazily, which means it would make another query to fetch the User when it's accessed.
    * JOIN FETCH tells JPA to fetch the User along with the GroupMember in a single query.
    * This reduces the number of database queries and improves performance.
     */
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user WHERE gm.group.id = :groupId")
    List<GroupMember> findGroupMembersWithUsersByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user.id = :userId")
    Group findGroupByUserId(@Param("userId") Long userId);
}
