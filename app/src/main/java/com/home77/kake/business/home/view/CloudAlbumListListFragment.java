package com.home77.kake.business.home.view;

import android.app.AlertDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.home77.common.base.collection.Params;
import com.home77.common.ui.util.SizeHelper;
import com.home77.common.ui.widget.Toast;
import com.home77.kake.App;
import com.home77.kake.R;
import com.home77.kake.business.home.adapter.CloudAlbumListAdapter;
import com.home77.kake.business.home.presenter.BaseFragment;
import com.home77.kake.business.home.presenter.CmdType;
import com.home77.kake.business.home.presenter.MsgType;
import com.home77.kake.business.home.presenter.ParamsKey;
import com.home77.kake.common.api.response.Album;
import com.home77.kake.common.event.BroadCastEvent;
import com.home77.kake.common.event.BroadCastEventConstant;
import com.home77.kake.common.widget.recyclerview.CloudAlbumGridItemDecoration;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author CJ
 */
public class CloudAlbumListListFragment extends BaseFragment {

  @BindView(R.id.recycler_view)
  RecyclerView recyclerView;
  @BindView(R.id.refresh_layout)
  SwipeRefreshLayout refreshLayout;
  Unbinder unbinder;
  private List<Album> albumList = new ArrayList<>();
  private CloudAlbumListAdapter cloudAlbumListAdapter;
  private AlertDialog alertDialog;

  public CloudAlbumListListFragment() {
    albumList.add(new Album());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    App.eventBus().unregister(this);
  }

  @Override
  public void executeCommand(CmdType cmdType, Params in, Params out) {
    switch (cmdType) {
      case VIEW_CREATE:
        View view = LayoutInflater.from(getContext())
                                  .inflate(R.layout.refreshable_recycler_layout, null, false);
        unbinder = ButterKnife.bind(this, view);
        App.eventBus().register(this);
        int spanCount = 3;
        int outerSpace = SizeHelper.dp(12);
        int innerSpace = SizeHelper.dp(6);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int widthAndHeight =
            (screenWidth - outerSpace * 2 - (spanCount - 1) * innerSpace) / spanCount;
        recyclerView.addItemDecoration(new CloudAlbumGridItemDecoration(outerSpace, innerSpace));
        cloudAlbumListAdapter = new CloudAlbumListAdapter(getContext(), albumList, widthAndHeight);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        recyclerView.setAdapter(cloudAlbumListAdapter);

        refreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            presenter.onMessage(MsgType.VIEW_REFRESH, null);
          }
        });
        break;
      case CLOUD_ALBUM_CREATING:
        alertDialog.dismiss();
      case CLOUD_ALBUM_LOADING:
        App.eventBus().post(new BroadCastEvent(BroadCastEventConstant.DIALOG_LOADING_SHOW, null));
        break;
      case CLOUD_ALBUM_CREATE_ERROR:
      case CLOUD_ALBUM_LOAD_ERROR:
        App.eventBus()
           .post(new BroadCastEvent(BroadCastEventConstant.DIALOG_LOADING_DISMISS, null));
        if (refreshLayout.isRefreshing()) {
          refreshLayout.setRefreshing(false);
        }
        String msg = in.get(ParamsKey.MSG, "");
        if (!TextUtils.isEmpty(msg)) {
          Toast.showShort(msg);
        }
        break;
      case CLOUD_ALBUM_CREATE_SUCCESS:
        App.eventBus()
           .post(new BroadCastEvent(BroadCastEventConstant.DIALOG_LOADING_DISMISS, null));
        if (refreshLayout.isRefreshing()) {
          refreshLayout.setRefreshing(false);
        }
        break;
      case CLOUD_ALBUM_LOAD_SUCCESS:
        List<Album> albumList = in.get(ParamsKey.ALBUM_LIST, null);
        this.albumList.clear();
        this.albumList.add(new Album());
        this.albumList.addAll(albumList);
        cloudAlbumListAdapter.notifyDataSetChanged();
        break;
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEvent(BroadCastEvent event) {
    switch (event.getEvent()) {
      case BroadCastEventConstant.DIALOG_UPLOAD_ALBUM:
        showUploadAlbumDialog();
        break;
    }
  }

  private void showUploadAlbumDialog() {
    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_album, null);
    alertDialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
    final EditText editText = (EditText) dialogView.findViewById(R.id.album_name_edit_text);
    dialogView.findViewById(R.id.ok_text_view).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String name = editText.getText().toString();
        presenter.onMessage(MsgType.CLICK_CREATE_ALBUM_DIALOG_OK,
                            Params.create(ParamsKey.ALBUM_NAME, name));
      }
    });
    dialogView.findViewById(R.id.cancel_text_view).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        alertDialog.dismiss();
      }
    });
    alertDialog.show();
  }
}
