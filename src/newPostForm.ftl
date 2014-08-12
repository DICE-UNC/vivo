			  <div class="posts">
				<div class="posts-title" id="posts-title">Discussions</div>
				<#if editable>
					<div class="posts-input" id="posts-input" style="display:none">
					  <input type="text" class="posts-input-title" id="posts-input-title" placeholder="title" />
					  <textarea rows="4" class="posts-input-content" id="posts-input-content" placeholder="content"></textarea>
					  <div class="posts-input-buttons">
					    <button type="button" id="posts-input-submit">Submit</button> 
					    <button type="button" id="posts-input-cancel">Cancel</button>
					  </div>
					</div>
				</#if>
				<div id="posts-updatable"></div>
				
			  </div>

<script language="javascript">

	  $(document).ready(function(){
	  	// init
		var posts_statements = [
		  <#list property.statements as statement>
			  <#include "${property.template}">
		  </#list>
		];

		var div=$('#posts-updatable');
		div.html(generateComments(posts_statements));
	  
		var input=$('#posts-input');
		var buttons = addEditButton($("#posts-title"), '${property.localName}', input);
		var title=$('#posts-input-title');
		var content=$('#posts-input-content');
		$('#posts-input-submit').button().click(function(event) {
			buttons.submit();
			var titleStr=title.val();
			var description=content.val();
			title.val('');
			content.val('');
			var owner="http://localhost/vivo/ontology/databook#rods";
			ajaxGet('/vivo/edit/databook/newRecord?subjectUri='+'${subjectUri[1]?js_string}'+
				'&predicateUri='+encodeURIComponent('${property.uri?js_string}')+
				'&owner='+encodeURIComponent(owner)+
				'&title='+encodeURIComponent(titleStr)+
				'&description='+encodeURIComponent(description),
				function(){
				  buttons.ret();
				  pullUpdate(
				    decodeURIComponent('${subjectUri[1]?js_string}'), 
				    '${property.uri?js_string}', 
				    posts_statements, 
				    div,
				    generateComment
				  );
			});					  
		  
		});
		$('#posts-input-cancel').button().click(function( event ) {
			buttons.cancel();
			title.val('');
			content.val('');
		});
	});
</script>
					
