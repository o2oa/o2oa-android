package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_cloud_disk_v3.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.CloudDiskActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.zone.ZoneActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go

class CloudDiskV3Activity : BaseMVPActivity<CloudDiskV3Contract.View, CloudDiskV3Contract.Presenter>() {



    override var mPresenter: CloudDiskV3Contract.Presenter = CloudDiskV3Presenter()

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar(getString(R.string.title_activity_yunpan), setupBackButton = true, isCloseBackIcon = true)

        rl_my_file.setOnClickListener {
            go<CloudDiskActivity>()
        }
        rl_org_file.setOnClickListener {
            go<ZoneActivity>()
        }
    }

    override fun layoutResId(): Int = R.layout.activity_cloud_disk_v3


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_scale_in, R.anim.activity_scale_out)
    }
}