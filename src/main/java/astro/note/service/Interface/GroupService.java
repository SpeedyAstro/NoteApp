package astro.note.service.Interface;

import astro.note.entity.Group;
import astro.note.entity.User;

import java.util.List;

public interface GroupService {
    List<Group> findGroupsByCreatorOrderByCreatedAtDesc(User user);
    Group findById(Long id);
    void saveGroup(Group group);
    void deleteGroup(Long id);
    Group findByGroupCode(String groupCode);
    List<Group> findGroupsByUserOrderByCreatedAtDesc(User user);
    Group findGroupByUserId(Long id);
}
