<#assign editable_short = [ "title", "description" ]?seq_contains(property.localName) && editable >

<div class="short-property" id="${property.localName}">
    <div class="short-property-name" id="${property.localName}-property-name">${property.name} <@p.verboseDisplay property /> </div>
    <div class="short-property-value" id="${property.localName}-display">
	<#if property.type == "data"> <#-- data property -->
		<#list property.statements as statement>
		    <#include "${property.template}">
		</#list> 
	<#else> <#-- object property -->
		<#list property.statements as statement>
		    <#include "${property.template}">
		</#list>
	</#if>
    </div>
    <#if editable_short>
	    <div style="display:none" id="${property.localName}-form">
	      <input type="text" name="newValue" id="${property.localName}-newValue"/>
	      <button type="button" id="${property.localName}-submit">Submit</button>
	      <button type="button" id="${property.localName}-cancel">Cancel</button>
	    </div>
    </#if>
</div>

<script language="javascript">
$(document).ready(function() {
    <#if editable_short>
    var localName = "${property.localName?js_string}";
    var propName = $("#"+localName + "-property-name");
    var propDisp = $("#"+localName + "-display");
    var propForm = $("#"+localName + "-form");
    var newValue = $("#"+localName + "-newValue");
  var funcs = addShortEditButton(propName, localName, propDisp, propForm);
  $("#"+localName+"-submit").button().click(function() {
    var newValueStr = newValue.val();
    funcs.submit();
    ajaxGet("/vivo/edit/databook/updateRecord?subjectUri="+"${subjectUri[1]?js_string}"+
	    "&predicateUri="+encodeURIComponent("${property.uri?js_string}")+
	    "&newValue="+encodeURIComponent(newValueStr),
	    function(data) {
	      propDisp.fadeOut(function() {
		funcs.ret();
		propDisp.text(newValueStr);
		propDisp.fadeIn();
	      });
	    });
  });
  $("#"+localName + "-cancel").button().click(function() {
    funcs.cancel();
  });
  
    </#if>
});
</script>


