package com.haitaos.finallbbs.service;

import com.haitaos.finallbbs.dto.*;
import com.haitaos.finallbbs.enums.SortEnum;
import com.haitaos.finallbbs.exception.CustomizeErrorCode;
import com.haitaos.finallbbs.exception.CustomizeException;
import com.haitaos.finallbbs.mapper.*;
import com.haitaos.finallbbs.model.*;
import com.haitaos.finallbbs.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class QuestionService {

    @Autowired
    private TimeUtils timeUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private UserAccountExtMapper userAccountExtMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionExtMapper questionExtMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ThumbMapper thumbMapper;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private CommentMapper commentMapper;



    @Autowired
    private Environment env;


    //@Value("${score1.publish.inc}")
    @Value("10")
    private Integer score1PublishInc;
//    @Value("${score2.publish.inc}")
    @Value("2")
    private Integer score2PublishInc;
//    @Value("${score3.publish.inc}")
    @Value("1")
    private Integer score3PublishInc;
//    @Value("${user.score1.priorities}")
    @Value("1")
    private Integer score1Priorities;
//    @Value("${user.score2.priorities}")
    @Value("5")
    private Integer score2Priorities;
//    @Value("${user.score3.priorities}")
    @Value("0")
    private Integer score3Priorities;



    public static List<String> getHtmlImageSrcList(String htmlText)
    {
        List<String> imgSrc = new ArrayList<String>();
        Matcher m = Pattern.compile("src=\"?(.*?)(\"|>|\\s+)").matcher(htmlText);
        while(m.find())
        {
            imgSrc.add(m.group(1));
        }
        return imgSrc;
    }

    public PaginationDTO listwithColumn(String search, String tag, String sort, Integer page, Integer size, Integer column2, UserAccount userAccount) {

        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays
                    .stream(tags)
                    .filter(StringUtils::isNotBlank)
                    .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("|"));
        }


        //
        Integer totalPage;
        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);
        if(column2!=null) questionQueryDTO.setColumn2(column2);
        if(StringUtils.isNotBlank(tag)){
            tag = tag.replace("+", "").replace("*", "").replace("?", "");
            questionQueryDTO.setTag(tag);
        }

        //  ColumnEnum columnEnum = ColumnEnum.

        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.name().toLowerCase(Locale.ENGLISH).equals(sort)) {
                questionQueryDTO.setSort(sort);

                if (sortEnum == SortEnum.HOT7) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7);
                }
                if (sortEnum == SortEnum.HOT30) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
                }
                break;
            }
        }


        Integer totalCount = questionExtMapper.countBySearch(questionQueryDTO);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        Integer offset = page < 1 ? 0 : size * (page - 1);
        questionQueryDTO.setSize(size);
        questionQueryDTO.setOffset(offset);
        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        PaginationDTO paginationDTO = new PaginationDTO();
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            UserAccountExample userAccountExample2 = new UserAccountExample();
            userAccountExample2.createCriteria().andUserIdEqualTo(user.getId());
            //   System.out.println("user.getId()："+user.getId());
            List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample2);
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user,userDTO);
            questionDTO.setUser(userDTO);
            questionDTO.setUserAccount(userAccounts.get(0));
            questionDTO.setUserGroupName(env.getProperty("user.group.r"+userAccounts.get(0).getGroupId()));
            questionDTO.setGmtLatestCommentStr(timeUtils.getTime(questionDTO.getGmtLatestComment(),null));
            String shortDescription=getShortDescription(questionDTO.getDescription(),questionDTO.getId());
            questionDTO.setShortDescription(shortDescription);
            if(questionDTO.getPermission()==0) questionDTO.setIsVisible(1);
            else if(userAccount!=null&&userAccount.getGroupId()>=questionDTO.getPermission()) questionDTO.setIsVisible(1);
            else questionDTO.setIsVisible(0);
            questionDTOList.add(questionDTO);
        }
        paginationDTO.setData(questionDTOList);


        paginationDTO.setPagination(totalPage,page);
        return paginationDTO;
    }


    public List<QuestionDTO> listTopwithColumn(String search, String tag, String sort, Integer column2) {

        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays
                    .stream(tags)
                    .filter(StringUtils::isNotBlank)
                    .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("|"));
        }
        //
        //  Integer totalPage;
        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        if(column2!=null) questionQueryDTO.setColumn2(column2);
        questionQueryDTO.setSearch(search);

        if(StringUtils.isNotBlank(tag)){
            tag = tag.replace("+", "").replace("*", "").replace("?", "");
            questionQueryDTO.setTag(tag);
        }


        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.name().toLowerCase(Locale.ENGLISH).equals(sort)) {
                questionQueryDTO.setSort(sort);

                if (sortEnum == SortEnum.HOT7) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7);
                }
                if (sortEnum == SortEnum.HOT30) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
                }
                break;
            }
        }

        List<Question> questions = questionExtMapper.selectTop(questionQueryDTO);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        //  PaginationDTO paginationDTO = new PaginationDTO();
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            UserAccountExample userAccountExample = new UserAccountExample();
            userAccountExample.createCriteria().andUserIdEqualTo(user.getId());
            List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user,userDTO);
            questionDTO.setUser(userDTO);
            questionDTO.setUserAccount(userAccounts.get(0));
            questionDTO.setGmtLatestCommentStr(timeUtils.getTime(questionDTO.getGmtLatestComment(),null));
            questionDTOList.add(questionDTO);
        }

        return questionDTOList;
    }


    public PaginationDTO listwithColumn(String search,Integer page, Integer size, String start, String end)
    {
        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays
                    .stream(tags)
                    .filter(StringUtils::isNotBlank)
                    .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("|"));
        }


        //
        Integer totalPage;
        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);
        if(start !="" && start !=null  ){questionQueryDTO.setStart(timeUtils.farmatTime2(start));}

        if(end !="" && end !=null ){questionQueryDTO.setEnd(timeUtils.farmatTime2(end));}


        Integer totalCount = questionExtMapper.countBySearchAll(questionQueryDTO);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        Integer offset = page < 1 ? 0 : size * (page - 1);
        questionQueryDTO.setSize(size);
        questionQueryDTO.setOffset(offset);
        List<Question> questions = questionExtMapper.selectBySearchAll(questionQueryDTO);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        PaginationDTO paginationDTO = new PaginationDTO();
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            UserAccountExample userAccountExample2 = new UserAccountExample();
            userAccountExample2.createCriteria().andUserIdEqualTo(user.getId());
            //   System.out.println("user.getId()："+user.getId());
            List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample2);
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user,userDTO);
            questionDTO.setUser(userDTO);
            questionDTO.setUserAccount(userAccounts.get(0));
            questionDTO.setUserGroupName(env.getProperty("user.group.r"+userAccounts.get(0).getGroupId()));
            questionDTO.setGmtCreatStr(timeUtils.getTime2(questionDTO.getGmtCreate(),"yyyy-MM-dd"));
            questionDTO.setGmtModifiedStr(timeUtils.getTime2(questionDTO.getGmtModified(),"yyyy-MM-dd"));
            questionDTO.setGmtLatestCommentStr(timeUtils.getTime2(questionDTO.getGmtLatestComment(),"yyyy-MM-dd"));
            String shortDescription=getShortDescription(questionDTO.getDescription(),questionDTO.getId());
            questionDTO.setShortDescription(shortDescription);
            questionDTOList.add(questionDTO);
        }
        paginationDTO.setData(questionDTOList);


        paginationDTO.setPagination(totalPage,page);
        return paginationDTO;
    }


        public String getShortDescription(String description,Long id){
        String shortDescription = description;
       /* shortDescription = shortDescription.replaceAll("<p id=\"descriptionP\" class=\"video\">","" );
        shortDescription = shortDescription.replaceAll("<p class=\"video\" id=\"descriptionP\">","" );
        shortDescription = shortDescription.replaceAll("<p id=\"descriptionP\">", "");
        shortDescription = shortDescription.replaceAll("</p>", "<br>");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("</h1>", "<br>");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("<h1>", "");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("</h2>", "<br>");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("<h2>", "");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("</h3>", "<br>");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("<h3>", "");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("<br><br>", "<br>");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("<p><br></p>", "");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("<p><br>", "<p>");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("<br></p>", "</p>");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("(?!<(br|p|img).*?>)<.*?>", ""); //剔出指定标签外的<iframe>的标签
        */
        List<String> imgSrc = getHtmlImageSrcList(shortDescription);
        /*shortDescription = shortDescription.replaceAll("(?!<(br|p).*?>)<.*?>", ""); //剔出指定标签外的<html>的标签
        shortDescription = shortDescription.replaceAll("&nbsp;", "");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("&nbsp;", "");//去除&nbsp;
        shortDescription = shortDescription.replaceAll("<pre>", "");
        shortDescription = shortDescription.replaceAll("</pre>", "");*/
        shortDescription = shortDescription.replaceAll("</?[^>]+>", "&nbsp;"); //剔出<html>的标签
        shortDescription = shortDescription.replaceAll("<a>\\s*|\t|\r|\n</a>", "&nbsp;");//去除字符串中的空格,回车,换行符,制表符
        if(shortDescription.length()>400) shortDescription=shortDescription.substring(0,400)+"...";
        if(imgSrc.size()==1) shortDescription=shortDescription+"<br><img style=\"max-width:49%;max-height:300px\" src=\""+imgSrc.get(0)+"\">";
        else if(imgSrc.size()>1){
            shortDescription=shortDescription+"<br><img style=\"max-width:49%;max-height:300px\" src=\""+imgSrc.get(0)+"\"><img style=\"max-width:49%;max-height:300px\" src=\""+imgSrc.get(1)+"\">";
        }
        return shortDescription;
    }

    public String getTextDescriptionFromHtml(String html){
        String textDescription = html.replaceAll("</?[^>]+>", ""); //剔出<html>的标签
        textDescription = textDescription.replaceAll("<a>\\s*|\t|\r|\n</a>", "");//去除字符串中的空格,回车,换行符,制表符
        textDescription = textDescription.replaceAll("&nbsp;", "");//去除&nbsp;
        return textDescription;
    }

    public QuestionDTO getById(Long id,Long viewUser_id) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);
        User user = userMapper.selectByPrimaryKey(question.getCreator());
        UserAccountExample userAccountExample = new UserAccountExample();
        userAccountExample.createCriteria().andUserIdEqualTo(user.getId());
        List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
        UserAccount userAccount = userAccounts.get(0);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        questionDTO.setUser(userDTO);
        questionDTO.setUserAccount(userAccount);
        questionDTO = setStatuses(questionDTO,viewUser_id);
        questionDTO.setGmtModifiedStr(timeUtils.getTime(questionDTO.getGmtModified(),null));
        Project project=projectService.findProjectByParentId(questionDTO.getId());
        questionDTO.setProject(project);
        return questionDTO;
    }

    public QuestionDTO setStatuses(QuestionDTO questionDTO,Long viewUser_id){
        questionDTO.setEdited(questionDTO.getGmtCreate().longValue()!=questionDTO.getGmtModified().longValue());
        if((questionDTO.getStatus()&1)==1) questionDTO.setEssence(true);
        if((questionDTO.getStatus()&2)==2) questionDTO.setSticky(true);
        if(viewUser_id.longValue()!=0L){
//            if(likeService.queryLike(questionDTO.getId(),1,viewUser_id)>0) questionDTO.setFavorite(true);
            if(userAccountService.isAdminByUserId(viewUser_id)){
                questionDTO.setCanClassify(true);
                questionDTO.setCanDelete(true);
                questionDTO.setCanEssence(true);
                questionDTO.setCanSticky(true);
                questionDTO.setCanEdit(true);
                questionDTO.setCanPromote(true);
            }else if(viewUser_id.longValue()==questionDTO.getCreator().longValue()){
                questionDTO.setCanEdit(true);
                questionDTO.setCanClassify(true);
                questionDTO.setCanDelete(true);
            }
        }

        return questionDTO;
    }

    public void createOrUpdate(Question question,UserDTO user) {
        if (question.getId() == 0) {
            // 创建
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            question.setGmtLatestComment(question.getGmtCreate());
            question.setViewCount(0);
            question.setLikeCount(0);
            question.setCommentCount(0);
            question.setStatus(0);
            questionMapper.insert(question);
            if(user.getVipRank()!=0){//VIP积分策略，可自行修改，这里简单处理
                score1PublishInc=score1PublishInc*2;
                score2PublishInc=score2PublishInc*2;
                score3PublishInc=1;
            }
            UserAccount userAccount = new UserAccount();
            userAccount.setUserId(question.getCreator());
            userAccount.setScore1(score1PublishInc);
            userAccount.setScore2(score2PublishInc);
            userAccount.setScore3(score3PublishInc);
            userAccount.setScore(score1PublishInc*score1Priorities+score2PublishInc*score2Priorities+score3PublishInc*score3Priorities);
            userAccountExtMapper.incScore(userAccount);
            userAccount=null;

        } else {
            // 更新
            question.setGmtModified(System.currentTimeMillis());
            QuestionExample example = new QuestionExample();
            example.createCriteria()
                    .andIdEqualTo(question.getId());
            int updated = questionMapper.updateByExampleSelective(question, example);
            if (updated != 1) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }

    public void createOrUpdate(Question question,UserDTO user,Long fileId) {
        if (question.getId() == 0) {
            // 创建
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            question.setGmtLatestComment(question.getGmtCreate());
            question.setViewCount(0);
            question.setLikeCount(0);
            question.setCommentCount(0);
            question.setStatus(0);
            questionMapper.insert(question);
            if(fileId!=null && fileId!=-1) {
                QuestionExample questionExample = new QuestionExample();
                questionExample.createCriteria()
                        .andCreatorEqualTo(question.getCreator())
                        .andGmtCreateEqualTo(question.getGmtCreate());
                List<Question> questions = questionMapper.selectByExample(questionExample);
                Project project = new Project();
                project.setParentId(questions.get(0).getId());
                ProjectExample projectExample = new ProjectExample();
                projectExample.createCriteria()
                        .andIdEqualTo(fileId);
                projectMapper.updateByExampleSelective(project, projectExample);
            }
            if(user.getVipRank()!=0){//VIP积分策略，可自行修改，这里简单处理
                score1PublishInc=score1PublishInc*2;
                score2PublishInc=score2PublishInc*2;
                score3PublishInc=1;
            }
            UserAccount userAccount = new UserAccount();
            userAccount.setUserId(question.getCreator());
            userAccount.setScore1(score1PublishInc);
            userAccount.setScore2(score2PublishInc);
            userAccount.setScore3(score3PublishInc);
            userAccount.setScore(score1PublishInc*score1Priorities+score2PublishInc*score2Priorities+score3PublishInc*score3Priorities);
            userAccountExtMapper.incScore(userAccount);
            userAccount=null;

        } else {
            // 更新
            question.setGmtModified(System.currentTimeMillis());
            QuestionExample example = new QuestionExample();
            example.createCriteria()
                    .andIdEqualTo(question.getId());
            int updated = questionMapper.updateByExampleSelective(question, example);
            if(fileId!=null && fileId!=-1) {
                Project project = new Project();
                project.setParentId(question.getId());
                ProjectExample projectExample = new ProjectExample();
                projectExample.createCriteria()
                        .andIdEqualTo(fileId);
                projectMapper.updateByExampleSelective(project, projectExample);
            }
            if (updated != 1) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }


    public List<QuestionDTO> selectRelated(QuestionDTO queryDTO) {
        if (StringUtils.isBlank(queryDTO.getTag())) {
            return new ArrayList<>();
        }
        String[] tags = StringUtils.split(queryDTO.getTag(), ",");
        String regexpTag = Arrays
                .stream(tags)
                .filter(StringUtils::isNotBlank)
                .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("|"));
        Question question = new Question();
        question.setId(queryDTO.getId());
        question.setTag(regexpTag);

        List<Question> questions = questionExtMapper.selectRelated(question);
        List<QuestionDTO> questionDTOS = questions.stream().map(q -> {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(q, questionDTO);
            return questionDTO;
        }).collect(Collectors.toList());
        return questionDTOS;
    }
    public PaginationDTO listByQuestionQueryDTO(QuestionQueryDTO questionQueryDTO) {
            return null;
    }


    public void incView(Long id) {
        Question question = new Question();
        question.setId(id);
        question.setViewCount(1);
        questionExtMapper.incView(question);

    }

    public int delQuestionById(Long user_id, Integer group_id,Long id) {
        int c=0;
        Question question = questionMapper.selectByPrimaryKey(id);
        if(group_id>=18){
            c=questionMapper.deleteByPrimaryKey(id);
        }
        else {
            QuestionExample questionExample = new QuestionExample();
            questionExample.createCriteria().andCreatorEqualTo(user_id).andIdEqualTo(id);
            c = questionMapper.deleteByExample(questionExample);
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(question.getCreator());
        userAccount.setScore1(score1PublishInc);
        userAccount.setScore2(score2PublishInc);
        userAccount.setScore3(score3PublishInc);
        userAccount.setScore(score1PublishInc*score1Priorities+score2PublishInc*score2Priorities+score3PublishInc*score3Priorities);
        userAccountExtMapper.decScore(userAccount);
        userAccount=null;

        return c;
    }

    public PaginationDTO listByUserId(Long userId, Integer page, Integer size) {

        Integer totalPage;
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria()
                .andCreatorEqualTo(userId);
        Integer totalCount = (int)questionMapper.countByExample(questionExample);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page > totalPage) {
            page = totalPage;
        }

        if (page < 1) {
            page = 1;
        }

        Integer offset = size * (page-1);
        QuestionExample example = new QuestionExample();
        example.createCriteria()
                .andCreatorEqualTo(userId);
        example.setOrderByClause("gmt_modified desc");
        List<Question> questions = questionMapper.selectByExampleWithRowbounds(example, new RowBounds(offset, size));
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        PaginationDTO paginationDTO = new PaginationDTO();
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user,userDTO);
            questionDTO.setUser(userDTO);
            questionDTO.setGmtModifiedStr(timeUtils.getTime(questionDTO.getGmtModified(),null));
            questionDTOList.add(questionDTO);
        }
        paginationDTO.setData(questionDTOList);
        paginationDTO.setTotalCount(totalCount);
        paginationDTO.setPagination(totalPage,page);
        return paginationDTO;

    }

    public PaginationDTO listByExample(Long userId, Integer page, Integer size, String likes) {
        Integer totalPage;
        ThumbExample thumbExample = new ThumbExample();
        thumbExample.createCriteria()
                .andLikerEqualTo(userId)
                .andTypeEqualTo(1);
        Integer totalCount = (int)thumbMapper.countByExample(thumbExample);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page > totalPage) {
            page = totalPage;
        }

        if (page < 1) {
            page = 1;
        }


        Integer offset = size * (page-1);

        thumbExample.setOrderByClause("gmt_modified desc");
        List<Thumb> thumbs = thumbMapper.selectByExampleWithRowbounds(thumbExample,new RowBounds(offset, size));
        List<Question> questionList = new ArrayList<>();
        PaginationDTO paginationDTO = new PaginationDTO();
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        Question questionTemp ;
        for (Thumb thumb : thumbs) {
            questionTemp=questionMapper.selectByPrimaryKey(thumb.getTargetId());
            if(questionTemp!=null)
                questionList.add(questionTemp);
        }
        for (Question question : questionList) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user,userDTO);
            questionDTO.setUser(userDTO);
            questionDTO.setGmtModifiedStr(timeUtils.getTime(questionDTO.getGmtModified(),null));
            questionDTOList.add(questionDTO);
        }

        paginationDTO.setData(questionDTOList);
        paginationDTO.setTotalCount(totalCount);
        paginationDTO.setPagination(totalPage,page);
        return paginationDTO;
    }

    public Question getQuestionById(Long id){
        return questionMapper.selectByPrimaryKey(id);
    }

    public int updateQuestion(Question question){
        return questionMapper.updateByPrimaryKeySelective(question);
    }

    public Map<String,Object> mapListPosts(Integer size){

        //轮播
        List<QuestionDTO> imagePosts=listImagePost(size);
        //最新发布
        List<QuestionDTO> newQuestions=listNewQuestion(size);
        //最新回复
        List<QuestionDTO> newComments=listNewComment(size);
        //热门排行
        List<QuestionDTO> hotQuestions=listHotQuestion(size);
        //精品帖子
        List<QuestionDTO> goodQuestions=listGoodQuestion(size);

        List<UserDTO> goodMans=listGoodMan(size);
        Map<String,Object> mapListPost=new HashMap<>();
        mapListPost.put("imagePosts",imagePosts);
        mapListPost.put("newQuestions",newQuestions);
        mapListPost.put("newComments",newComments);
        mapListPost.put("hotQuestions",hotQuestions);
        mapListPost.put("goodQuestions",goodQuestions);
        mapListPost.put("goodMans",goodMans);
        return mapListPost;
    }

    public List<QuestionDTO>  listNewQuestion(Integer size){
        List<Question> newQuestion= questionExtMapper.selectLatest(size);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question question : newQuestion) {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            questionDTO.setGmtModifiedStr(timeUtils.getTime2(questionDTO.getGmtModified(),"yyyy-MM-dd"));
            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }

    public List<QuestionDTO>  listNewComment(Integer size){
        List<Question> newComment= questionExtMapper.selectLatestComment(size);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question question : newComment) {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            questionDTO.setGmtLatestCommentStr(timeUtils.getTime2(questionDTO.getGmtLatestComment(),"yyyy-MM-dd"));
            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }


    public List<QuestionDTO>  listHotQuestion(Integer size){
        List<Question> hotQuestion= questionExtMapper.selectHotQuestion(size);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question question : hotQuestion) {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            questionDTO.setGmtModifiedStr(timeUtils.getTime2(questionDTO.getGmtModified(),"yyyy-MM-dd"));
            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }

    public List<QuestionDTO>  listGoodQuestion(Integer size){
        List<Question> goodQuestion= questionExtMapper.selectGoodQuestion(size);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question question : goodQuestion) {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            questionDTO.setGmtModifiedStr(timeUtils.getTime2(questionDTO.getGmtModified(),"yyyy-MM-dd"));
            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }


    public List<UserDTO>  listGoodMan(Integer size){
        List<Long> creators= questionExtMapper.selectGoodMan(size);
        List<UserDTO> userDTOList = new ArrayList<>();
        for (Long creator : creators) {
            UserDTO userDTO = new UserDTO();
            UserExample userExample=new UserExample();
            userExample.createCriteria().andIdEqualTo(creator);
            List<User> user = userMapper.selectByExample(userExample);
            BeanUtils.copyProperties(user.get(0),userDTO);
           UserAccountExample userAccountExample=new UserAccountExample();
           userAccountExample.createCriteria().andUserIdEqualTo(creator);
           List<UserAccount> userAccounts=userAccountMapper.selectByExample(userAccountExample);
           userDTO.setUserAccount(userAccounts.get(0));
            userDTOList.add(userDTO);
        }
        return userDTOList;
    }

    public long getColumnCount(int i){
        QuestionExample questionExample=new QuestionExample();
        questionExample.createCriteria()
                .andColumn2EqualTo(i);
        return questionMapper.countByExample(questionExample);
    }

    public List<QuestionDTO>  listImagePost(Integer size){
        QuestionExample dbquestion=new QuestionExample();
        dbquestion.createCriteria()
                .andColumn2EqualTo(8);
        dbquestion.setOrderByClause("gmt_create desc");

        RowBounds rowBounds=new RowBounds(0,size);
        List<Question> goodQuestion= questionMapper.selectByExampleWithRowbounds(dbquestion,rowBounds);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question question : goodQuestion) {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            questionDTO.setGmtModifiedStr(timeUtils.getTime2(questionDTO.getGmtModified(),"yyyy-MM-dd"));
            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }

    public void delQuestion(Long userId){
        QuestionExample questionExample=new QuestionExample();
        questionExample.createCriteria()
                .andCreatorEqualTo(userId);
        questionMapper.deleteByExample(questionExample);
        CommentExample commentExample=new CommentExample();
        commentExample.createCriteria()
                .andCommentatorEqualTo(userId);
        commentMapper.deleteByExample(commentExample);

    }

    public ArrayList<Long> countQuestion(){
        ArrayList<Long> res=new ArrayList<Long>();
        try {
            List<Date> dates=timeUtils.getZhou();
            for(Date date :dates){
               QuestionQueryDTO questionQueryDTO=new QuestionQueryDTO();
               System.out.println(date.getTime());
                questionQueryDTO.setStart(date.getTime());
                questionQueryDTO.setEnd(date.getTime()+3600*1000*24);
                res.add( Long.valueOf(questionExtMapper.countBySearchAll(questionQueryDTO)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return res;
    }





}
