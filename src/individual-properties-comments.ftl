<#--
Copyright (c) 2012, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-->

<#-- Template for property listing on individual profile page -->

<#import "lib-properties.ftl" as p>
<#assign subjectUri = individual.controlPanelUrl()?split("=") >
<#assign shortPropertyList = [ "created", "dataSize", "owner", "partOf", "description", "session", "title" ] >
<#assign buttonPropertyList = [ "likedBy", "dislikedBy", "sharedTo" ] >
<#assign bigPropertyList = [ "accessHistory", "sessionPart", "discussion" ] >

<#list propertyGroups.all as group>
    <#assign groupName = group.getName(nameForOtherGroup)>
    <#assign verbose = (verbosePropertySwitch.currentValue)!false>
    
    <script id="post-template" type="text/html">
    <div class="post">
      <div class="post-title">
	  <#= statement.title || "no title" #>
      </div>
      <div class="post-user">
	<# if(statement.owner !== undefined) { #>
	  <a href="/vivo/individual?uri=<#= encodeURIComponent(statement.owner) #>">
	    <#= statement.ownerLabel || statement.owner.substring(statement.owner.indexOf("#")+1) #>
	  </a>
	<# } else { #>
	  no post user
	<# } #>
      </div>
      <div class="post-time">
	<#= statement.created || "no post time #>
      </div>
      <div class="post-content">
	<# if(statement.description !== undefined { #>
	  <pre><#= statement.description #></pre>
	<# } else { #>
	  no description
	<# } #>
      </div>
    </div>
    </script>
    
    <section class="property-group" role="region">
        <nav class="scroll-up" role="navigation">
            <a href="#branding" title="scroll up">
                <img src="${urls.images}/individual/scroll-up.gif" alt="scroll to property group menus" />
            </a>
        </nav>
        
        <#-- Display the group heading --> 
        <#if groupName?has_content>
    		<#--the function replaces spaces in the name with underscores, also called for the property group menu-->
        	<#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
            <h2 id="${groupNameHtmlId}">${groupName?capitalize}</h2>
        <#else>
            <#-- Databook: don't show this heading <h2 id="properties">Properties</h2> -->
        </#if>
        
        <#-- List the properties in the group -->
        <#list group.properties as property>
		<#if shortPropertyList?seq_contains(property.localName) >
		        <div class="short-property" id="${property.localName}">
			<div class="short-property-name">${property.name} <@p.verboseDisplay property /> </div>
			<div class="short-property-value">
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
			</div>
		</#if>
		<#if buttonPropertyList?seq_contains(property.localName) >
			<div class="button-property" id="${property.localName}">
			<div class="button-property-name">${property.name} 
			<#assign url = property.addUrl>
			<#if url?has_content>
				<a class="add-${property.localName}"
					title="Add new ${property.localName?lower_case} entry">
				<img class="add-individual" id="add-${property.localName}"
					src="${urls.images}/individual/addIcon.gif" alt="add" />
				<img class="add-individual" id="add-${property.localName}-pac"
					src="${urls.images}/icons/pacman.gif" style="display:none; height: 15px; width: 15px" alt="add" /></a>
				<script langauge="javascript">
					$("#add-${property.localName}").click(function() {
						var div=$('#buttun-property-value-${property.localName}');
						var add=$('#add-${property.localName}');
						var pac=$('#add-${property.localName}-pac');
						add.hide();
						pac.show();
						ajaxGet('/vivo/edit/databook/newRecord${url?substring(url?index_of("?"))?js_string}',function(data){
							add.show();
							pac.hide();
							div.fadeOut(function() {
								div.text(data);
							}).fadeIn();
						});
					}); 
				</script>
			</#if>


			<@p.verboseDisplay property /> </div>
			<div class="button-property-value" id="buttun-property-value-${property.localName}">
				    ${property.statements?size}
			</div>
			</div>			
		</#if>
	</#list>
        <#-- List the properties in the group -->
        <#list group.properties as property>
		<#if bigPropertyList?seq_contains(property.localName) >
			<#if property.localName == "discussion">
			<#assign url = property.addUrl>
			  <#-- discussion widget -->
			  <div class="posts">
			      <#if editable>
				<div class="posts-title">Discussions
				  <a class="add-${property.localName}"
					  title="Add new ${property.localName?lower_case} entry">

				  <img class="add-individual" id="add-${property.localName}"
					    src="${urls.images}/individual/addIcon.gif" alt="add" />
				  <img class="add-individual" id="add-${property.localName}-pac"
					    src="${urls.images}/icons/pacman.gif" style="display:none; height: 15px; width: 15px" alt="add" /></a>
				</div>
				<div class="posts-input" id="posts-input" style="display:none">
				  <input type="text" class="posts-input-title" id="posts-input-title" placeholder="title" />
				  <textarea rows="4" class="posts-input-content" id="posts-input-content" placeholder="content"></textarea>
				  <div class="posts-input-buttons">
				    <button type="button" id="posts-input-submit">Submit</button> 
				    <button type="button" id="posts-input-cancel">Cancel</button>
				  </div>
				</div>
				<script langauge="javascript">
				var posts_statements = [
				<#list property.statements as statement>
					<#include "${property.template}">
				</#list>
				];

					var div=$('#posts-updatable');
					var input=$('#posts-input');
					var add=$('#add-${property.localName}');
					var pac=$('#add-${property.localName}-pac');
					var title=$('#posts-input-title');
					var content=$('#posts-input-content');
					$('#posts-input-submit').button().click(function(event) {
						input.slideUp();
						add.hide();
						pac.show();
						var titleStr=title.val();
						var description=content.val();
						title.val('');
						content.val('');
						var owner="http://localhost/vivo/ontology/databook#rods";
						ajaxGet('/vivo/edit/databook/newRecord${url?substring(url?index_of("?"))?js_string}'+
							'&owner='+encodeURIComponent(owner)+
							'&title='+encodeURIComponent(titleStr)+
							'&description='+encodeURIComponent(description),
							function(){
							  add.show();
							  pac.hide();
							  pullCommentsUpdate(lastUpdate, div);
						});					  
					  
					});
					$('#posts-input-cancel').button().click(function( event ) {
						input.slideUp();
						title.val('');
						content.val('');
					});
					$("#add-${property.localName}").click(function() {
						input.slideDown();
					}); 
					div.html(generateComments(posts_statements));
				</script>
			      </#if>
			      <div id="posts-updatable">
			      </div>
			  </div>
			<#else>
			  <#-- start javascript widgets -->
			  <div id="tabs">
				  <ul>
					  <li><a href="#accessHistoryTimelineDiv">Timeline</a></li>
					  <li><a href="#accessHistoryLineChartDiv">Line Chart</a></li>
				  </ul>
				  <div id="accessHistoryTimelineDiv"> 
					  <div id="accessHistoryTimeline" style="height:490px"> timeline goes here </div>
				  </div>
				  <div id="accessHistoryLineChartDiv">
					  <div id="accessHistoryLineChart" style="height:290px"> line chart goes here </div>
					  <form>
						  <div id="lineChartRadios">
							  <input type="radio" id="lineChartRadio1" name="lineChartRadioGroup" value="Week"/><label for="lineChartRadio1">Week</label>
							  <input type="radio" id="lineChartRadio2" name="lineChartRadioGroup" value="Month" /><label for="lineChartRadio2">Month</label>
							  <input type="radio" id="lineChartRadio3" name="lineChartRadioGroup" value="Year" checked="checked" /><label for="lineChartRadio3">Year</label>
						  </div>
						  <div id="lineChartAggreRadios">
							  <input type="radio" id="lineChartAggreRadio1" name="lineChartAggreRadioGroup" value="By Minute"/><label for="lineChartAggreRadio1">By Minute</label>
							  <input type="radio" id="lineChartAggreRadio2" name="lineChartAggreRadioGroup" value="By Hour" /><label for="lineChartAggreRadio2">By Hour</label>
							  <input type="radio" id="lineChartAggreRadio3" name="lineChartAggreRadioGroup" value="By Day" checked="checked" /><label for="lineChartAggreRadio3">By Day</label>
							  <input type="radio" id="lineChartAggreRadio4" name="lineChartAggreRadioGroup" value="By Month" /><label for="lineChartAggreRadio4">By Month</label>
						  </div>
						  <div id="lineChartTypeRadios">
							  <input type="radio" id="lineChartTypeRadio1" name="lineChartTypeRadioGroup" value="Overall" checked="checked" /><label for="lineChartTypeRadio1">Overall</label>
							  <input type="radio" id="lineChartTypeRadio2" name="lineChartTypeRadioGroup" value="Read/Write" /><label for="lineChartTypeRadio2">Read/Write</label>
						  </div>
					  </form>
				  </div>
			  </div>
			  <script>
				  google.load("visualization", "1", {packages:["corechart"]});
				  google.setOnLoadCallback(drawAccessHistory);

				  function drawAccessHistory() {
					  var data = new google.visualization.DataTable({
						  cols: [
							  {id: "start", label: "start", type: "datetime"},
							  {id: "end", label: "end", type: "datetime"},
							  {id: "content", label: "content", type: "string"},
							  {id: "action", label: "action", type: "string"}
						  ],
						  rows: [
							  <#list property.statements as statement>
								  <#include "${property.template}">
							  </#list>
						  ]});

					  drawAccessHistoryFromData(data);
				  }
			  </script>
			</#if>
		</#if>
	</#list>
        <#-- List the properties in the group -->
        <#list group.properties as property>
		<#if !shortPropertyList?seq_contains(property.localName) && !buttonPropertyList?seq_contains(property.localName) && !bigPropertyList?seq_contains(property.localName) >
            <article class="property" role="article">
		        <#-- Property display name -->
		        <#if property.localName == "authorInAuthorship" && editable && (publicationCount > 0) >
		            <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> 
		                <a id="managePropLink" class="manageLinks" href="${urls.base}/managePublications?subjectUri=${subjectUri[1]!}" title="manage publications" <#if verbose>style="padding-top:10px"</#if> >
		                    manage publications
		                </a>
		            </h3>
		        <#elseif property.localName == "hasResearcherRole" && editable && (grantCount > 0) >
		        <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> 
		            <a id="manageGrantLink" class="manageLinks" href="${urls.base}/manageGrants?subjectUri=${subjectUri[1]!}" title="manage grants & projects" <#if verbose>style="padding-top:10px"</#if> >
		                manage grants & projects
		            </a>
		        </h3>
		        <#else>
		            <h3 id="${property.localName}">${property.name} <@p.addLink property editable /> <@p.verboseDisplay property /> </h3>
		        </#if>

		        <#-- List the statements for each property -->
		        <ul class="property-list" role="list" id="${property.localName}List">
		            <#-- data property -->
		            <#if property.type == "data">
		                <@p.dataPropertyList property editable />
		            <#-- object property -->
		            <#else>
		                <@p.objectProperty property editable />
		            </#if>
		        </ul>
            </article> <!-- end property -->
		</#if>

	
        </#list>
    </section> <!-- end property-group -->
</#list>
