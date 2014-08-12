def vivo_dir = '/home/a/Downloads/vivo-rel-1.5'
def src_dir = 'src'
def icon_dir = '/usr/share/icons/gnome/32x32'

def iconDir = new File('$vivo_dir/productMods/images/icons')
def databookFormsDir = new File('$vivo_dir/productMods/templates/freemarker/databook/forms/')
// remove databook source
task clean << {
  delete '$vivo_dir/src/databook'
}

task createDir << {
  
  if(!iconDir.isDirectory()) {
    iconDir.mkdirs()
  }
  if(!databookFormsDir.isDirectory()) {
    databookFormsDir.mkdirs()
  }
}

task copyProjectFiles(dependsOn: [clean, createDir]) << {
  copy {
    from '$src_dir/*'
    into '$vivo_dir/src/'
  }
  copy {
	  from '$src_dir/startup_listeners.txt'
	  into '$vivo_dir/productMods/WEB-INF/resources'
  }

  // copy data size list view files
  copy {
	  from '$src_dir/dataSizeListView.n3'
	  into '$vivo_dir/productMods/WEB-INF/ontologies/app/loadedAtStartup'
  }
  copy {
	  from '$src_dir/propStatement-dataSize.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/body/partials/individual'
  }
  copy {
	  from '$src_dir/listViewConfig-dataSize.xml'
	  into '$vivo_dir/productMods/config'
  }

  // copy access history list view files
  copy {
	  from '$src_dir/accessHistoryListView.n3'
	  into '$vivo_dir/productMods/WEB-INF/ontologies/app/loadedAtStartup'
  }
  copy {
	  from '$src_dir/propStatement-accessHistory.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/body/partials/individual'
  }
  copy {
	  from '$src_dir/listViewConfig-accessHistory.xml'
	  into '$vivo_dir/productMods/config'
  }

  // copy session part list view files
  copy {
	  from '$src_dir/sessionPartListView.n3'
	  into '$vivo_dir/productMods/WEB-INF/ontologies/app/loadedAtStartup'
  }

  // copy discussion list view files
  copy {
	  from '$src_dir/discussionListView.n3'
	  into '$vivo_dir/productMods/WEB-INF/ontologies/app/loadedAtStartup'
  }
  copy {
	  from '$src_dir/propStatement-discussion.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/body/partials/individual'
  }
  copy {
	  from '$src_dir/listViewConfig-discussion.xml'
	  into '$vivo_dir/productMods/config'
  }

  // copy hasPart list view files
  copy {
	  from '$src_dir/hasPartListView.n3'
	  into '$vivo_dir/productMods/WEB-INF/ontologies/app/loadedAtStartup'
  }
  copy {
	  from '$src_dir/propStatement-hasPart.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/body/partials/individual'
  }
  copy {
	  from '$src_dir/listViewConfig-hasPart.xml'
	  into '$vivo_dir/productMods/config'
  }
  // copy databook js
  copy {
	  from '$src_dir/databook.js'
	  into '$vivo_dir/productMods/js'
  }

  // copy databook css
  copy {
	  from '$src_dir/databook.css'
	  into '$vivo_dir/productMods/css'
  }

  // copy icons
  copy {
	  from '$icon_dir/actions/document-new.png'
	  into '$vivo_dir/productMods/images/icons/create.png'
  }
  copy {
	  from '$icon_dir/actions/document-open.png'
	  into '$vivo_dir/productMods/images/icons/open.png'
  }
  copy {
	  from '$icon_dir/mimetypes/ascii.png'
	  into '$vivo_dir/productMods/images/icons/read.png'
  }
  copy {
	  from '$icon_dir/apps/accessories-text-editor.png'
	  into '$vivo_dir/productMods/images/icons/write.png'
  }
  copy {
	  from '$icon_dir/places/folder.png'
	  into '$vivo_dir/productMods/images/icons/close.png'
  }
  copy {
	  from '$icon_dir/actions/window-close.png'
	  into '$vivo_dir/productMods/images/icons/delete.png'
  }
  copy {
	  from '$icon_dir/actions/document-save.png'
	  into '$vivo_dir/productMods/images/icons/update.png'
  }
  copy {
	  from '$icon_dir/actions/document-save-as.png'
	  into '$vivo_dir/productMods/images/icons/overwrite.png'
  }
  copy {
	  from '$icon_dir/actions/forward.png'
	  into '$vivo_dir/productMods/images/icons/move.png'
  }
  copy {
	  from '$icon_dir/actions/system-run.png'
	  into '$vivo_dir/productMods/images/icons/rule.png'
  }
  copy {
	  from '$icon_dir/emblems/emblem-system.png'
	  into '$vivo_dir/productMods/images/icons/microservice.png'
  }
  copy {
	  from '$icon_dir/actions/document-save-as.png'
	  into '$vivo_dir/productMods/images/icons/put.png'
  }
  copy {
	  from '$icon_dir/emblems/emblem-downloads.png'
	  into '$vivo_dir/productMods/images/icons/get.png'
  }
  copy {
	  from '$src_dir/images/ajax-loader.gif'
	  into '$vivo_dir/productMods/images/icons/pacman.gif'
  }
  copy {
	  from '$icon_dir/places/folder.png'
	  into '$vivo_dir/productMods/images/icons/coll.png'
  }
  copy {
	  from '$icon_dir/mimetypes/ascii.png'
	  into '$vivo_dir/productMods/images/icons/data.png'
  }

  copy {
	  from '$src_dir/newDataObjForm.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/databook/forms/'
  }
  copy {
	  from '$src_dir/newPostForm.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/databook/forms/'
  }
  copy {
	  from '$src_dir/newShortForm.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/databook/forms/'
  }
  copy {
	  from '$src_dir/newRecordPartialAjaxRet.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/databook/'
  }
  copy {
	  from '$src_dir/updateRecordPartialAjaxRet.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/databook/'
  }
  copy {
	  from '$src_dir/readRecordPartialAjaxRet.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/databook/'
  }

  copy {
	  from '$src_dir/web.xml'
	  into '$vivo_dir/productMods/WEB-INF'
  }

  // copy individual ftl
  copy {
	  from '$src_dir/individual.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/body/individual'
  }
  copy {
	  from '$src_dir/individual-properties.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/body/partials/individual/individual-properties.ftl'
  }
  copy {
	  from '$src_dir/individual-vitro.ftl'
	  into '$vivo_dir/productMods/templates/freemarker/body/individual/individual-vitro.ftl'
  }
}

// copy timeline css and js files
task copyTimelineFiles << {
  // copy {
  //	from '$src_dir/timeline/timeline.css'
  //	into '$vivo_dir/productMods/css'
  // }
  // copy {
  //	from '$src_dir/timeline/timeline.js'
  //	into '$vivo_dir/productMods/js'
  // }
  copy {
	  from '$src_dir/timeline'
	  into '$vivo_dir/productMods'
  }
}

// copy jquery-file-upload plugin
task copyJqueryFileUploadPluginFiles << {
  copy {
	  from '$src_dir/fileupload'
	  into '$vivo_dir/productMods'
  }
}

// copy moment js
task copyMomentJsFiles << {
  copy {
	  from '$src_dir/moment.js'
	  into '$vivo_dir/productMods/js'
  }
}

def dependencies = [ copyProjectFiles, copyMomentJsFiles, copyTimelineFiles, copyJqueryFileUploadPluginFiles ]
task deploy(type: Exec, dependsOn: dependencies) {
  workingDir '$vivo_dir'
  commandLine 'ant deploy'
    //store the output instead of printing to the console:
  standardOutput = new ByteArrayOutputStream()

  //extension method stopTomcat.output() can be used to obtain the output:
  ext.output = {
    return standardOutput.toString()
  }
}

task compile(type: Exec, dependsOn: dependencies) {
  workingDir '$vivo_dir'
  commandLine 'ant all'
    //store the output instead of printing to the console:
  standardOutput = new ByteArrayOutputStream()

  //extension method stopTomcat.output() can be used to obtain the output:
  ext.output = {
    return standardOutput.toString()
  }
}
