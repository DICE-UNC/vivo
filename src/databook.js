function toDateMoment(currDate) {
	return moment([currDate.getFullYear(), currDate.getMonth(), currDate.getDate()]);
}

// start is a moment object, step is a duration object
function aggregateData(data, start, step, overall) {	
	var dailyAccess = new Array();
	var dataAggre = new google.visualization.DataTable();

	var oa = overall === "Overall";
	function resetAccess(access) {
		if(oa) {
			access["access"] = 0;
		} else {
			access["read"] = 0;
			access["write"] = 0;
			access["other"] = 0;
                        access["get"] = 0;
			access["put"] = 0;
			access["metadata"] = 0;
		}

	}

	function incAccess(action, access) {
		if(oa) {
	    		access["access"]++;
		} else {
	    		var accessCategory;
			if(action.indexOf("read") != -1) {
				accessCategory = "read";
			} else if(action.indexOf("get") != -1) {
				accessCategory = "get";
			} else if(action.indexOf("write") != -1) {
				accessCategory = "write";
			} else if(action.indexOf("put") != -1) {
				accessCategory = "put";
			} else if(action.indexOf("metadata") != -1) {
				accessCategory = "metadata";
			} else {
				accessCategory = "other";
			}
			access[accessCategory]++;
		}

	}

	function addRow(dataAggre, date, access) {
		if(oa) {
			dataAggre.addRow([date.toDate(), access["access"]]);
		} else {
			dataAggre.addRow([date.toDate(), access["read"], access["write"], access["get"], access["put"], access["metadata"], access["other"]]);
		}

	}

	dataAggre.addColumn("date", "start");
	if(oa) {
		dataAggre.addColumn("number", "access");
	} else {
		dataAggre.addColumn("number", "read");
		dataAggre.addColumn("number", "write");
		dataAggre.addColumn("number", "get");
		dataAggre.addColumn("number", "put");
		dataAggre.addColumn("number", "metadata");
		dataAggre.addColumn("number", "other");
	}
	
	var rowIndexes = data.getSortedRows(0);
	resetAccess(dailyAccess);
	var currDate = start;
        var i = 0;
	// skip rows before currDate
        while(i < rowIndexes.length && moment(data.getValue(rowIndexes[i], 0)).isBefore(currDate)) {
                i++;
        }
	
	// add row for the previous step
	addRow(dataAggre, currDate.clone().subtract(step), dailyAccess);

        var nextStepDate = currDate.clone().add(step);
	for(; i < rowIndexes.length; i++) {
	    var nextDate = moment(data.getValue(rowIndexes[i], 0));
	    if(nextDate.isBefore(nextStepDate)) {
	    	// same date
		incAccess(data.getValue(rowIndexes[i], 3), dailyAccess);
	    } else {
	    	// new date
	    	// add row for the start date 
	    	addRow(dataAggre, currDate.clone(), dailyAccess);
	    	// add rows for dates in between
		resetAccess(dailyAccess);
		currDate.add(step);
		nextStepDate.add(step);
	   	while(!nextDate.isBefore(nextStepDate)) {
	    	    addRow(dataAggre, currDate.clone(), dailyAccess);
		    currDate.add(step);
		    nextStepDate.add(step);
	    	}
	    	// set up the new current date
	    	incAccess(data.getValue(rowIndexes[i], 3), dailyAccess);
	    }
	}

	// add current date
	addRow(dataAggre, currDate.clone(), dailyAccess);
				
	// add row to today
	resetAccess(dailyAccess);
        addRow(dataAggre, currDate.clone().add(step), dailyAccess);
        //var today = moment();
	//for(currDate.add(step); currDate.isBefore(today); currDate.add(step)) {
	//    addRow(dataAggre, currDate.clone(), dailyAccess);
	//}
	return dataAggre;
}
var timeline;
var currdata;
function drawAccessHistoryTimeline(data) {
        if(!timeline) {
                timeline = new links.Timeline(document.getElementById("accessHistoryTimeline"));
        };
	options = {
	  width:  "100%",
	  height: "99%",
	  style: "box"
	};
        
        if(currdata && JSON.stringify(currdata) === JSON.stringify(data)) { 
        } else {
	        currdata = data;
                timeline.draw(data, options);
        }
}

function drawAccessHistoryLineChart(data) {
	var options = {
		hAxis: { // show one year
			viewWindowMode: "explicit",
			viewWindow: {
				max: moment().toDate()
			}
		},
		vAxis: { // min = 0
			viewWindow: {
				min: 0
			}
		}
	};
	
	$( "#lineChartRadios" ).buttonset();
	$( "#lineChartAggreRadios" ).buttonset();
	$( "#lineChartTypeRadios" ).buttonset();
	
	var lineChart = new google.visualization.LineChart(document.getElementById("accessHistoryLineChart"));
	var window = "Year";
	var aggre = "By Day";
	var overall = "Overall";
	
	function drawLineChart(val, aggre, overall) {
		var start;
		var step;
		// assume that data has been sorted by column 0
		var startDate = data.getValue(0, 0);

		if(aggre === "By Minute") {
			start = moment([startDate.getFullYear(), startDate.getMonth(), startDate.getDate(), startDate.getHours(), startDate.getMinutes()]);
			step = moment.duration(1, "m");
		} else if(aggre === "By Hour") {
			start = moment([startDate.getFullYear(), startDate.getMonth(), startDate.getDate(), startDate.getHours()]);
			step = moment.duration(1, "h");
		} else if(aggre === "By Day") {
			start = moment([startDate.getFullYear(), startDate.getMonth(), startDate.getDate()]);
			step = moment.duration(1, "d");
		} else if(aggre === "By Month") {
			start = moment([startDate.getFullYear(), startDate.getMonth()]);
			step = moment.duration(1, "M");
		}
		var dataAggre = aggregateData(data, start, step, overall);
		if(val === "Hour") {
                        duration = moment.duration(1,"h");
                } else if (val === "Day") {
                        duration = moment.duration(1,"d");
                } else if(val === "Week") {
			duration = moment.duration(1, "w");
		} else if(val === "Month") {
			duration = moment.duration(1, "M");
		} else if(val === "Year") {
			duration = moment.duration(1, "y");
		}
		options.hAxis.viewWindow.min = moment().subtract(duration).toDate();
                //options.isStacked = true;
		lineChart.draw(dataAggre, options);
	}

	// set button actions
	$( "input:radio[name=lineChartRadioGroup]" ).change(function() {
		window = $(this).val();
		drawLineChart(window, aggre, overall);
	});
	
	$( "input:radio[name=lineChartAggreRadioGroup]" ).change(function() {
		aggre = $(this).val();
		drawLineChart(window, aggre, overall);
	});

	$( "input:radio[name=lineChartTypeRadioGroup]" ).change(function() {
		overall = $(this).val();
		drawLineChart(window, aggre, overall);
	});

	$( "#lineChartRadio3" ).trigger("click");
	$( "#lineChartAggreRadio3" ).trigger("click");
	$( "#lineChartTypeRadio1" ).trigger("click");
	
	drawLineChart(window, aggre, overall);
	
}

function drawAccessHistoryFromData(data) {
	// draw tabs
	$( "#tabs" ).tabs();
	
	// timeline					
	drawAccessHistoryTimeline(data);

	// only draw once					
	var draw = true;

	$("#tabs").tabs({
		activate: function(event, ui) {
			if(draw && ui.newTab.index() === 1) {
				draw = false;
	
				// line chart					
				drawAccessHistoryLineChart(data);
				
			}
		}
	});

}

// client side template engine 
// original version: 
// http://www.west-wind.com/weblog/posts/2008/Oct/13/Client-Templating-with-jQuery

var _tmplCache = {}
function parseTemplate(str, data) {
    /// <summary>
    /// Client side template parser that uses &lt;#= #&gt; and &lt;# code #&gt; expressions.
    /// and # # code blocks for template expansion.
    /// NOTE: chokes on single quotes in the document in some situations
    ///       use &amp;rsquo; for literals in text and avoid any single quote
    ///       attribute delimiters.
    /// </summary>    
    /// <param name="str" type="string">The text of the template to expand</param>    
    /// <param name="data" type="var">
    /// Any data that is to be merged. Pass an object and
    /// that object's properties are visible as variables.
    /// </param>    
    /// <returns type="string" />  
    var err = "";
    try {
        var func = _tmplCache[str];
        if (!func) {
            var strFunc =
            "var p=[],print=function(){p.push.apply(p,arguments);};" +
                        "with(obj){p.push('" +

            str.replace(/[\r\t\n]/g, " ")
               .replace(/'(?=[^#]*#>)/g, "\t")
               .split("'").join("\\'")
               .split("\t").join("'")
               .replace(/<#=(.+?)#>/g, "',$1,'")
               .split("<#").join("');")
               .split("#>").join("p.push('")
               + "');}return p.join('');";
//alert(strFunc);
            func = new Function("obj", strFunc);
            _tmplCache[str] = func;
        }
        return func(data);
    } catch (e) { err = e.message; }
    return "< # ERROR: " + $('<div/>').text(err).html() + " # >";
}
// generate comments

function generateComments(statements) {
  var html="";
  for(var i = 0; i < statements.length; i++) {
    html += generateComment(statements[i]);
  }
  return html;
}

function generateHasParts(statements) {
  var html="";
  for(var i = 0; i < statements.length; i++) {
    html += generateHasPart(statements[i]);
  }
  return html;
}

function generateComment(statement) {
  var titlediv = '<div class="post-title">' +
	  (statement.title || "no title") +
      '</div>';
      
  var ownerLabel = statement.ownerLabel || statement.owner && statement.owner.substring(statement.owner.indexOf("#")+1);
  var userdiv = '<div class="post-user">' +
	(statement.owner !== null?
	  '<a href="/vivo/individual?uri=' + encodeURIComponent(statement.owner) +'">' +
	    ownerLabel +
	  '</a>'
	:
	  'no post user')+
      '</div>';
	  
  var timediv = 
     '<div class="post-time">'+
	(statement.created || "no post time") +
      '</div>';
  var contentdiv = 
  '<div class="post-content">' +
	(statement.description !== null?
	  '<pre>'+ statement.description +'</pre>'
	:
	  'no description')+
      '</div>';

 // don't break the line between return and the return value otherwise javascript will return undefined
  return '<div class="post" id="' + uriToId(statement.uri) + '">' + titlediv + userdiv + timediv + contentdiv + '</div>';
}

function generateHasPart(statement) {
  var src;
  var title;
  switch(statement.type) {
    case "Data Object":
      src = "/vivo/images/icons/data.png";
      title = statement.type;
      break;
    case "Collection":
      src = "/vivo/images/icons/coll.png";
      title = statement.type;
      break;
    default:
      src = "/vivo/images/icons/unknown.png";
      title = "";
      break;
  }
  var titlediv = '<div class="post-title">' +
	  '<img class="databook-image-small" title="'+title+'" src="' + src + '"/>' +
      '</div>';
      
  var ownerLabel = statement.ownerLabel || statement.owner && statement.owner.substring(statement.owner.lastIndexOf("/")+1);
  var userdiv = '<div class="post-user">' +
	(statement.owner !== null?
	  '<a href="/vivo/individual?uri=' + encodeURIComponent(statement.owner) +'">' +
	    ownerLabel +
	  '</a>'
	:
	  'unknown owner')+
      '</div>';
	  
  var contentdiv = 
  '<div class="post-content">' +
	(statement.label !== null?
	  '<a href="/vivo/individual?uri=' + encodeURIComponent(statement.uri) +'">' +
	    statement.label.substring(statement.label.lastIndexOf("/") + 1) +
	  '</a>'
	:
	  'unknown label')+
      '</div>';

 // don't break the line between return and the return value otherwise javascript will return undefined
  return '<div class="post" id="' + uriToId(statement.uri) + '">' + titlediv + userdiv + contentdiv + '</div>';
}

function ajaxGet(url, handler) {
  $.get(url,
    function(data){
      handler(data.substring(
		data.indexOf("<ret>")+"<ret>".length, 
		data.lastIndexOf("</ret>")
      ));
  });					  
}

function ajaxGetJson(url, req, handler) {
  $.post(url, JSON.stringify(req),
    function(data){
      handler(data);
  });
}
var MAX_RES_SIZE = 100000000;
function buildEsQuery(uri, linkprop) {
        var q1 = {};

        q1[linkprop]=uri;
                                   
        var q = {
                "from": 0,
                "size": MAX_RES_SIZE, 
                "_source": {
                    "exclude": ["fulltext"]
                },
                "sort": {
                    "created" : {"order":"asc"}
                },
                "query": {
                    "bool": {
                        "must": [{
                            "range": {
                                "created": {
                                    "from": moment().subtract(1, "year").valueOf(),
                                    "to": moment().valueOf()
                                }
                            }
                        }, {
                            "match": {
                                "type": "databook.persistence.rule.rdf.ruleset.Access"
                            }
                        }, {
                            "term": q1               
                        }]
                    }
                }
        };
        return q;
}

function refreshDraw(uri, type) {
        var eshost = "localhost";
        var query = buildEsQuery(uri, type === "user"?"linkingUser.user.uri":"linkingDataEntity.dataEntity.uri");
				      ajaxGetJson("http://"+eshost+":9200/databook/entity/_search", query, function(data0) {
				          // extract from data0 rows
				          var rows = esToGoogleTable(data0);
                          var data = new google.visualization.DataTable({
                              cols: [
                                  {id: "start", label: "start", type: "datetime"},
                                  {id: "end", label: "end", type: "datetime"},
                                  {id: "content", label: "content", type: "string"},
                                  {id: "action", label: "action", type: "string"}
                              ],
                              rows: rows
                          });
                          drawAccessHistoryFromData(data);
                      
                      })
}
function esToGoogleTable(data0) {
  // target format
  var rows = [];
  for(var i = 0;i<data0.hits.hits.length;i++) {
          var hit = data0.hits.hits[i]._source;
                //alert(hit);
          rows.push({c:[
                {v: moment(hit.created).toDate()}, // start
                {}, // finish
                {v: "<a class='timeline-event-link' href='individual?uri=file://"+hit.uri+"'>"+hit.description+"</a>"}, // description
                {v: hit.title} // access type
          ]});
  }
  //alert(rows);
  return rows;
}

function DescIterator(list) {
  this.list = list;
  this.n = list.length - 1;
}
DescIterator.prototype.next = function () {
     var st = this.n >= 0? this.list[this.n] : undefined;
      this.n--;
      return st;
   
}
function uriToId(uri) {
  return encodeURIComponent(uri).replace(/%/g,"_");
}

// load new posts in the the existing posts
function pullUpdate(subjectUri, predicateUri, statements, div, genFunc) {
  var url="/vivo/databook/readRecord?subjectUri="+encodeURIComponent(subjectUri)+"&predicateUri="+encodeURIComponent(predicateUri);
  ajaxGet(url, function(data) {
    var startIndex = data.indexOf("<script>")+"<script>".length;
    var endIndex = data.indexOf("</script>");
    var json = "("+data.substring(startIndex, endIndex)+")"; // text must be wrapped in parentheses to avoid syntax error
//    alert(json);
    var newStatements = eval(json); 
    var children = div.children();
    // assume that both statements and newStatements are sorted in desc order of the created field
    // start from earliest post, and the uri is sorted too
    var newItr = new DescIterator(newStatements);
    var oldItr = new DescIterator(statements);
    var newStmt = newItr.next();
    var oldStmt = oldItr.next();
    while(newStmt) {
      if(!oldStmt || newStmt.uri !== oldStmt.uri) {
	var html = genFunc(newStmt);
	var sdiv = $(html);
	sdiv.hide();
	if(oldStmt) {
	  $("#"+uriToId(oldStmt.uri)).after(sdiv);
	} else {
	  div.prepend(sdiv);
	}
	sdiv.slideDown();
	newStmt = newItr.next();
      } else {
	oldStmt = oldItr.next();
	newStmt = newItr.next();
      }
    }
    statements.length = 0;
    for(var i = 0;i<newStatements.length; i++) {
      statements.push(newStatements[i]);
    }
  });
}


function addEditButton(elem, localName, form) {
    var add = $('<img id="add-'+localName+'" src="/vivo/images/individual/addIcon.gif" class="databook-image-small" alt="add" />');
    var pac = $('<img id="add-'+localName+'-pac" src="/vivo/images/icons/pacman.gif" class="databook-image-small" style="display:none" alt="working..." /></a>');
    var a = $('<a class="add-'+localName+'" title="Add new '+localName+' entry"/>');
    a.append(add, pac);
     elem.append(a);
     add.click(function() {
	    form.slideDown();
    });
    
return {submit: function() {
  			form.slideUp();
			add.hide();
			pac.show();
}, ret: function() {
			add.show();
			pac.hide();
},cancel:function() {
			form.slideUp();
  
}};

 
}

function addShortEditButton(elem, localName, display, form) {
    var edit = $('<img id="add-'+localName+'" src="/vivo/images/individual/editIcon.gif" class="databook-image-small" alt="edit" />');
    var pac = $('<img id="add-'+localName+'-pac" src="/vivo/images/icons/pacman.gif" class="databook-image-small" style="display:none" alt="working..." /></a>');
    var a = $('<a class="edit-'+localName+'" title="Edit '+localName+' entry"/>');
    a.append(edit, pac);
    elem.append(a);
    edit.click(function() {
      display.hide();
      form.show();
    });
     
return {submit: function() {
			form.hide();
			display.show();
			edit.hide();
			pac.show();
}, ret: function() {
			edit.show();
			pac.hide();
},cancel:function() {
			form.hide();
			display.show();
  
}};

 
}
