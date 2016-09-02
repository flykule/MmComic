package com.example.castle.mmcomic.parser;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by castle on 16-8-30.
 * 文件解析接口
 */
public abstract class BaseParser<T> {

    //用于判断是否读取完成的标志位
    private boolean mComplete = false;
    /**
     * 解析资源的方法，可能根据情况抛出io异常
     *
     * @param file 要解析的目标
     * @throws IOException
     */
    public abstract void parse(File file) throws IOException;

    /**
     * 销毁数据
     *
     * @throws IOException
     */
    public abstract void destroy() throws IOException;

    /**
     * 返回资源类型的后缀，例如zip,tar或者目录等等
     *
     * @return 资源后缀
     */
    public abstract String getType();

    /**
     * 返回特定页面
     *
     * @param num 要访问的页面
     * @return 指定页面
     * @throws IOException
     */
    public abstract InputStream getPage(int num) throws IOException;

    /**
     * 返回页数
     *
     * @return 资源有效页数
     */
    public abstract int pageCount();

    /**
     * 通过rxjava过滤观察数据
     *
     * @param dataList 原始数据集合
     */
    void subscribeData(final List<T> dataList) {
        Observable.from(dataList)
                .filter(new Func1<T, Boolean>() {
                    @Override
                    public Boolean call(T t) {
                        return fileFilter(t);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .onBackpressureBuffer()
                //.unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<T>() {
                    @Override
                    public void onCompleted() {
                        setComplete(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.getMessage());
                    }

                    @Override
                    public void onNext(T t) {
                        whenNext(t);
                    }
                });
    }

    public boolean isComplete() {
        return mComplete;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    /**
     * 文件过滤器，判断数据的合法性
     *
     * @param t 观察到的发射数据
     * @return 数据是否合法
     */
    abstract boolean fileFilter(T t);

    /**
     * 观察到一条合法数据时调用的方法
     *
     * @param t 数据
     */
    abstract void whenNext(T t);

}
