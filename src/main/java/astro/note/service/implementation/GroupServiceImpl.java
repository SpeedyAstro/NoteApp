package astro.note.service.implementation;

import astro.note.Repository.GroupMemberRepository;
import astro.note.Repository.GroupRepository;
import astro.note.entity.Group;
import astro.note.entity.User;
import astro.note.service.Interface.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    public List<Group> findGroupsByCreatorOrderByCreatedAtDesc(User user) {
        return groupRepository.findByCreatorOrderByCreatedAtDesc(user);
    }

    @Override
    public Group findById(Long id) {
        return groupRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void saveGroup(Group group) {
        groupRepository.save(group);
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    @Override
    public Group findByGroupCode(String groupCode) {
        return groupRepository.findByGroupCode(groupCode);
    }

    @Override
    public List<Group> findGroupsByUserOrderByCreatedAtDesc(User user) {
        List<Group> groups = groupMemberRepository.findGroupsByUser(user);
        groups.sort((g1, g2) -> g2.getCreatedAt().compareTo(g1.getCreatedAt()));
        return groups;
    }

    @Override
    public Group findGroupByUserId(Long id) {
        return groupMemberRepository.findGroupByUserId(id);
    }
}
