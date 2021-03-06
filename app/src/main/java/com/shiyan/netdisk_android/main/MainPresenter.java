/*
 * MIT License
 *
 * Copyright (c) 2017 石岩
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.shiyan.netdisk_android.main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shiyan.netdisk_android.SecuDiskApplication;
import com.shiyan.netdisk_android.data.DataRepoImpl;
import com.shiyan.netdisk_android.data.DataSource;
import com.shiyan.netdisk_android.model.UserFile;
import com.shiyan.netdisk_android.utils.Utils;
import com.vincent.filepicker.filter.entity.BaseFile;
import com.vincent.filepicker.filter.entity.VideoFile;

import java.util.List;

/**
 * Contact shiyan233@hotmail.com
 * Blog    https://saltyx.github.io
 */

public class MainPresenter implements MainContract.Presenter {

    final String TAG = getClass().getName();

    private DataRepoImpl mDataRepo;
    private MainContract.View mMainView;

    public MainPresenter(DataRepoImpl mDataRepo, MainContract.View mMainView) {
        this.mDataRepo = mDataRepo;
        this.mMainView = mMainView;

        mMainView.setPresenter(this);
    }

    @Override
    public void start() {
        setRoot();
    }

    @Override
    public void set(int folderId) {

        mDataRepo.getFilesByFolder(folderId, new DataSource.GetData<String>() {
            @Override
            public void onLoaded(String data) {
                mMainView.showFiles(data);
                Log.i(TAG, "onLoaded: ".concat(data));
            }

            @Override
            public void onDataNotAvailable(@Nullable String msg) {
                mMainView.userFeedBack(msg, MainContract.FEED_BACK_SNACKBAR_LONG);
                mMainView.refresh(false);
            }
        });

    }

    @Override public void setRoot() {
        mDataRepo.getFilesByFolder(1, new DataSource.GetData<String>() {
            @Override
            public void onLoaded(String data) {
                mMainView.showFiles(data);
            }

            @Override
            public void onDataNotAvailable(@Nullable String msg) {
                mMainView.userFeedBack(msg, MainContract.FEED_BACK_SNACKBAR_LONG);
                mMainView.refresh(false);
            }
        });
    }

    @Override
    public void change() {
        mMainView.toggle();
    }

    @Override
    public void delete(@NonNull final UserFile file) {
        final DataSource.ResultCallBack callBack = new DataSource.ResultCallBack() {
            @Override
            public void onSuccess(@Nullable String success) {
                mMainView.userFeedBack("delete success!",MainContract.FEED_BACK_TOAST_SHORT);
                mMainView.remove(file);
            }

            @Override
            public void onError(@Nullable String error) {
                mMainView.userFeedBack(error, MainContract.FEED_BACK_SNACKBAR_LONG);
            }
        };

        if (file.isFolder()) {
            mDataRepo.deleteFolder(file.getId(), callBack);
        } else {
            mDataRepo.deleteFiles(file.getId(), callBack);
        }
    }

    @Override
    public void rename(final UserFile file) {
        final DataSource.ResultCallBack callback = new DataSource.ResultCallBack() {
            @Override public void onSuccess(@Nullable String success) {
                mMainView.userFeedBack("success", MainContract.FEED_BACK_TOAST_SHORT);
                mMainView.rename(file);
            }

            @Override public void onError(@Nullable String error) {
                mMainView.userFeedBack(error, MainContract.FEED_BACK_SNACKBAR_LONG);
            }
        };
        if (file.isFolder()) {
            mDataRepo.updateFolder(file.getId(), file.getFileName(),callback);
        } else {
            mDataRepo.updateFile(file.getId(), file.getFileName(), callback);
        }
    }

    @Override
    public void shareOrCancel(UserFile file) {
        final DataSource.ResultCallBack callback = new DataSource.ResultCallBack() {
            @Override public void onSuccess(@Nullable String success) {

            }

            @Override public void onError(@Nullable String error) {

            }
        };
        if (file.isFolder()) {
            if (file.isShared()) {
                mDataRepo.cancelShare(file.getId(), callback);
            } else {
                mDataRepo.shareFile(file.getId(), callback);
            }
        } else {
            if (file.isShared()) {
                mDataRepo.cancelShare(file.getId(), callback);
            } else {
                mDataRepo.shareFile(file.getId(), callback);
            }
        }
    }

    @Override
    public void encryptOrDecrypt(UserFile file, String passPhrase) {
        final DataSource.ResultCallBack callback = new DataSource.ResultCallBack() {
            @Override public void onSuccess(@Nullable String success) {
                mMainView.userFeedBack("success", MainContract.FEED_BACK_TOAST_SHORT);
            }

            @Override public void onError(@Nullable String error) {
                mMainView.userFeedBack(error, MainContract.FEED_BACK_SNACKBAR_LONG);
            }
        };
        if (file.isFolder()) {
            if (file.isEncrypted()) {
                mDataRepo.decryptFolder(file.getId(), passPhrase, callback);
            } else {
                mDataRepo.encryptFolder(file.getId(), passPhrase, callback);
            }
        } else {
            if (file.isEncrypted()) {
                mDataRepo.decryptFile(file.getId(), passPhrase, callback);
            } else {
                mDataRepo.encryptFile(file.getId(), passPhrase, callback);
            }
        }
    }

    @Override public void createFolder(final UserFile file) {
        mDataRepo.createFolder(file.getFileName(), file.getFromFolder(), new DataSource.ResultCallBack() {
            @Override public void onSuccess(@Nullable String success) {
                mMainView.addFolder(file);
                mMainView.userFeedBack("success",MainContract.FEED_BACK_TOAST_SHORT);
            }

            @Override public void onError(@Nullable String error) {
                mMainView.userFeedBack(error, MainContract.FEED_BACK_TOAST_LONG);
            }
        });
    }

    @Override public void goToNextFolder(UserFile file) {
        if (file != null) {
            SecuDiskApplication.goToNext(file.getId());
            if (mMainView.getTitle() == null) {
                mMainView.setTitle(file.getFileName());
            } else {
                mMainView.setTitle(mMainView.getTitle().concat(">>").concat(file.getFileName()));
            }
            mMainView.showOrHideRecentFile(false);
            set(SecuDiskApplication.CurrentFolder);
        }
    }

    @Override public int backToPrevious() {
        if (SecuDiskApplication.CurrentFolder != 1) {
            //not root
            int id = SecuDiskApplication.findPreFolder();
            if (id == 1) {
                //pre node is root
                mMainView.setTitle("Recent");
                mMainView.showOrHideRecentFile(true);
                setRoot();
            } else {
                String title = mMainView.getTitle();
                String newTitle = "";
                String [] temp = title.split(">>");
                for (int i = 0; i < temp.length-1; i++) {
                    newTitle = newTitle.concat(temp[i]).concat(">>");
                }
                mMainView.setTitle(newTitle);set(id);
            }
            Log.i(TAG, "backToPrevious: ".concat(String.valueOf(id)));
            return id;
        } else {
            return -1;
        }
    }

    @Override public void uploadCommonFile(List<BaseFile> files) {
        for (BaseFile file: files) {
            String[] data = file.getPath().split("/");
            String name = data[data.length-1];
            final UserFile file1 = new UserFile();
            file1.setFileName(name);
            file1.setFileSize(file.getSize());
            file1.setRemark(file.getPath());
            file1.setFromFolder(SecuDiskApplication.CurrentFolder);
            file1.setCreateAt(Utils.getNowTime());
            file1.setUpdateAt(Utils.getNowTime());
            mDataRepo.createFile(file1, new DataSource.ResultCallBack() {
                @Override public void onSuccess(@Nullable String success) {
                    mMainView.userFeedBack("success!", MainContract.FEED_BACK_TOAST_SHORT);
                    mMainView.add(file1);
                }

                @Override public void onError(@Nullable String error) {
                    mMainView.userFeedBack(error, MainContract.FEED_BACK_TOAST_LONG);
                }
            });
        }
    }
}
