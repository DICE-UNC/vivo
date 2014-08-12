<#-- Current version based on http://tutorialzine.com/2013/05/mini-ajax-file-upload-form/ -->

<div id="data-input" style="display: none">        
	        <div class="posts-input-buttons">
	<form class="editForm" action = "edit/databook/upload" enctype="multipart/form-data" method="post">
	    	
        	  <input type="file" name="file" id="file" style="display: none" />
		  <button type="button" id="data-object" >Data Object</button>
	</form>
        <form class="editForm" id="coll-form" action = "edit/databook/upload" method="post">
		  <input type="text" name="coll" id="coll"/>
		  <button type="button" id="collection" >Collection</button>
	</form>
            
        	  <button type="button" id="data-cancel">Cancel</button>
		</div>
        
</div>


<script>
/* function setDisabled(id, disabled) {
	$(id).attr("disabled", disabled);
}
function disable(id) {
	setDisabled(id, true);
}
function enable(id) {
	setDisabled(id, false);
}
function update() {
	var radio_button = $("#data-object");
	var file = radio_button.attr("checked"); // change to prop for 1.6+
	if(!file) {
		disable("#file");enable("#coll");		
	} else {
		enable("#file");disable("#coll");
	}
}*/
$(document).ready(function() {
var funcs = addEditButton($("#data-title"), "${property.localName}", $("#data-input"));
var doneFunc = function(e, data) {
	funcs.ret();
	pullUpdate(
	  decodeURIComponent('${subjectUri[1]?js_string}'), 
	  '${property.uri?js_string}', 
	  hasParts_statements, 
	  divParts,
	  generateHasPart
	);

    };
  var subjectUri = decodeURIComponent('${subjectUri[1]?js_string}');

  $('#hidden').val(subjectUri);
  $('#data-cancel').button().click(function( event ) {
	  funcs.cancel();
  });
  $("#collection").button().click(function() {
    funcs.submit();
    $.ajax({
      method: 'POST',
      url: "/vivo/edit/databook/upload", 
      data: {subjectUri: subjectUri, dataType: "Collection", coll: $("#coll").val()}
      })
      .done(doneFunc)
      .fail(function() {
	funcs.ret();
      });
  })
  $("#data-object").button().click(function() {
    $("#file").click();
  });
  // var div=$("hasParts-updatable");
  $("#data-input").fileupload({
    add: function(e, data) {
      funcs.submit();
      data.formData = { subjectUri: subjectUri, dataType: "Data Object" };
      data.submit();
      
    },
    done: doneFunc,
    fail: function(e, data) {
      funcs.ret();
    }
  })
		
});
</script>
