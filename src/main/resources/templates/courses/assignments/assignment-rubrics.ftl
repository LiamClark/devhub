[#import "../../macros.ftl" as macros]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("assignments.title") ]
<style type="text/css">
    <link rel="stylesheet" href="/static/vendor/angular-chart/dist/angular-chart.min.css">
    body > .angular-bootstrap-contextmenu.dropdown {
        width: 300px !important;
    }
</style>
[/@macros.renderHeader]
[@macros.renderMenu i18n user /]

<div class="container" ng-controller="StatisticsControl">

    <ol class="breadcrumb">
        <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
        <li><a href="${course.course.getURI()}">${course.course.code} - ${course.course.name}</a></li>
        <li>
          <span uib-dropdown dropdown-append-to-body="true">
            <a href id="simple-dropdown" uib-dropdown-toggle>
						${course.timeSpan.start?string["yyyy"]}[#if course.timeSpan.end??] - ${course.timeSpan.end?string["yyyy"]}[/#if]
                            <span class="caret"></span>
            </a>
            <ul uib-dropdown-menu>
			[#list course.course.getEditions() as a]
                <li><a href="${a.getURI()}">${a.timeSpan.start?string["yyyy"]}[#if a.timeSpan.end??] - ${a.timeSpan.end?string["yyyy"]}[/#if]</a></li>
			[/#list]
            </ul>
          </span>
        </li>
        <li><a href="${course.getURI()}">${ i18n.translate("assignments.title") }</a></li>
        <li>
				<span uib-dropdown dropdown-append-to-body="true">
					<a href id="simple-dropdown" uib-dropdown-toggle>
					${assignment.getName()}
                        <span class="caret"></span>
					</a>
					<ul uib-dropdown-menu>
					[#list course.getAssignments() as a]
                        <li><a href="${a.getURI()}rubrics">${a.getName()}</a></li>
					[/#list]
					</ul>
				</span>
        </li>
        <li>
				<span uib-dropdown>
					<a href id="simple-dropdown" uib-dropdown-toggle>
						Rubrics
						<span class="caret"></span>
					</a>
					<ul uib-dropdown-menu>
						<li><a href="${assignment.getURI()}">Overview</a></li>
					</ul>
				</span>
        </li>
    </ol>

	<div class="row">
		<div class="col-md-8">

            <div class="panel panel-default">
                <div class="panel-heading" ng-bind="assignment.name"></div>

                <table class="table table-bordered">
                    <thead>
                    <tr context-menu="contextMenuForAssignment()">
                        <th>Name</th>
                        <th>Weight</th>
                        <th>Correlation</th>
                        <th>Mastery</th>
                        <th>Points</th>
                        <th>Count</th>
                    </tr>
                    </thead>
                    <tbody>
                    [#--we need to add tasks to the assignment so we need the button for now.--]
                    [#--<tr ng-if="!assignment.tasks.length">--]
                    <tr>
                        <td colspan="6">
                            <em>Looks like you have no tasks yet. <a id="new-task-button" ng-click="addTask()" style="cursor: pointer;">Want to create one?</a></em>
                        </td>
                    </tr>
                    </tbody>
                    <tbody ng-repeat="task in assignment.tasks">
                    <tr class="active"  context-menu="contextMenuForTask(task)">
                        <td>
                            <a editable-text="task.description" ng-bind="task.description"></a>
                        </td>
                        <td>
                            <strong ng-bind="task.totalWeight"></strong>
                        </td>
                        <td colspan="2"></td>
                        <td><strong ng-bind="task.totalAchievablePoints"></strong> <em>({{task.totalAchievablePointsWithWeight}})</em></td>
                        <td></td>
                    </tr>
                    <tr ng-repeat-start="characteristic in task.characteristics"  context-menu="contextMenuForCharacteristic(task, characteristic)">
                        <td rowspan="{{ characteristic.levels.length }}"><a editable-textarea="characteristic.description" ng-bind="characteristic.description"></a></td>
                        <td rowspan="{{ characteristic.levels.length }}"><a editable-number="characteristic.weight" ng-bind="characteristic.weight"></a></td>
                        <td rowspan="{{ characteristic.levels.length }}" ng-bind="characteristic.correlation"></td>
                        <td><a editable-textarea="characteristic.levels[0].description" ng-bind="characteristic.levels[0].description"></a></td>
                        <td><a editable-number="characteristic.levels[0].points" ng-bind="characteristic.levels[0].points"></a></td>
                        <td ng-bind="characteristic.levels[0].count"></td>
                    </tr>
                    <tr ng-repeat-end ng-repeat="level in characteristic.levels" ng-hide="$first" context-menu="contextMenuForCharacteristic(task, characteristic)">
                        <td><a editable-textarea="level.description" ng-bind="level.description"></a></td>
                        <td><a editable-number="level.points" ng-bind="level.points"></a></td>
                        <td ng-bind="level.count"></td>
                    </tr>
                    </tbody>
                </table>
            </div>
		</div>
		<div class="col-md-4">
			<div class="panel panel-default">
				<div class="panel-heading">Histogram</div>
        <div class="panel-body">
            <canvas id="bar" class="chart chart-bar" chart-data="data" chart-labels="labels" chart-series="[assignment.name]" ng-if="data.length && labels.length"></canvas>
        </div>
				<table class="table table-bordered">
					<tr>
						<th>Mean</th>
						<td ng-bind="mean"></td>
					</tr>
					<tr>
						<th>Median</th>
						<td ng-bind="median"></td>
					</tr>
					<tr>
						<th>Achievable Points</th>
						<td ng-bind="assignment.totalAchievablePoints"></td>
					</tr>
          <tr>
            <th>Published</th>
            <td>
              <a href="${assignment.getURI()}edit">
                <span class="label" ng-class="{ 'label-success' : assignment.gradesReleased, 'label-info': !assignment.gradesReleased}"
                      ng-bind=" assignment.gradesReleased ? 'Published' : 'Not published'"></span>
              </a>
            </td>
          </tr>
				</table>
			</div>
		</div>
	</div>


</div>
[@macros.renderScripts]
<script src="/static/vendor/angular/angular.min.js"></script>
<script src="/static/vendor/angular-bootstrap/ui-bootstrap.min.js"></script>
<script src="/static/vendor/angular-xeditable/dist/js/xeditable.min.js"></script>
<script src="/static/vendor/angular-bootstrap-contextmenu/contextMenu.js"></script>
<script src="/static/vendor/jstat/dist/jstat.min.js"></script>
<script src="/static/vendor/Chartjs/Chart.min.js"></script>
<script src="/static/vendor/angular-chart/dist/angular-chart.js"></script>
<script src="/static/js/assignment-rubrics.js"></script>

[/@macros.renderScripts]
[@macros.renderFooter /]
