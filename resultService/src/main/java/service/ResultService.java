package service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import dao.ResultDao;
import domain.Result;
import grpc.interceptor.BearerAuthHolder;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ResultService {

    @Inject
    ResultDao resultDao;

    @Inject
    BearerAuthHolder authHolder;

    @Transactional
    public Result create(Result result) {
        log.info("creating studentId=".concat(result.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        resultDao.persist(result);
        return result;
    }

    @Transactional
    public Result read(String studentId) {
        log.info("reading studentId=".concat(studentId));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        return resultDao.findByStudentId(studentId);
    }

    @Transactional
    public Result update(Result result) {
        log.info("updating studentId=".concat(result.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        Result updatingResult = resultDao.findByStudentId(result.getStudentId());
        if (updatingResult==null) {
            throw new NoSuchElementException("Unknown Result with studentId=".concat(result.getStudentId()));
        }//if
        else {
            updatingResult.setArt(result.getArt());
            updatingResult.setChemistry(result.getChemistry());
            updatingResult.setMaths(result.getMaths());
            resultDao.persist(updatingResult);
        }//else
        return updatingResult;
    }

    @Transactional
    public long delete(String studentId) {
        log.info("deleting studentId=".concat(studentId));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        Result result = resultDao.findByStudentId(studentId);
        if (result==null) {
            throw new NoSuchElementException("Unknown Result with studentId=".concat(studentId));
        }//if
        else {
            return resultDao.delete("studentId", studentId);
        }//else
    }

    @Transactional
    public List<Result> listAll() {
        log.info("listing All");
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        List<Result> resultList = resultDao.listAll();
        return resultList;
    }

}
