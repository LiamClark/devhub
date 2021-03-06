[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.courses") /]
[@macros.renderMenu i18n user /]
<div class="container">

[#if user.isAdmin()]
    <h2>Courses
        <a href="/courses/setup" class="btn btn-success btn-sm pull-right">
            <i class="glyphicon glyphicon-plus-sign"></i> ${ i18n.translate("course.set-up") }
        </a>
    </h2>
    <table class="table table-bordered">
        <tbody>
[#assign administratingCourses = courses.listAll()]
    [#if administratingCourses?has_content ]
        [#list administratingCourses as course]
        <tr>
            <td>
                <a href="${course.getURI()}">${course.code} - ${course.name}</a>
            </td>
        </tr>
        [/#list]
    [#else]
        <tr>
            <td class="muted">${i18n.translate("course.no-courses")}</td>
        </tr>
    [/#if]
        </tbody>
    </table>
[#else]
    <h2>My courses</h2>
    <table class="table table-bordered">
        <tbody>
            [#assign groups=user.listGroups()]
            [#if groups?has_content]
                [#list groups as group]
                <tr>
                    <td>
                        <a href="${group.getURI()}">${group.getGroupName()}</a>
                    </td>
                </tr>
                [/#list]
            [#else]
            <tr>
                <td class="muted">
                ${i18n.translate("block.my-projects.empty-list")}
                </td>
            </tr>
            [/#if]
        </tbody>
    </table>

    [#assign assistingCourses=courseEditions.listAssistingCourses(user)]
    [#if assistingCourses?has_content]
        <h2>${i18n.translate("block.assisting-projects.title")}</h2>
        <table class="table table-bordered">
            <tbody>
                [#list assistingCourses as courseEdition ]
                <tr>
                    <td>
                        <a href="${courseEdition.getURI()}">${courseEdition.getName()}</a>
                    </td>
                </tr>
                [/#list]
            </tbody>
        </table>
    [/#if]

    [#assign availableCourses=courseEditions.listNotYetParticipatedCourses(user)]
    [#if availableCourses?has_content]
        <h2>Available courses</h2>
        <table class="table table-bordered">
            <tbody>
                [#list availableCourses as courseEdition ]
                <tr>
                    <td>
                        ${courseEdition.getName()}
                        <a href="${courseEdition.getURI()}enroll" class="btn btn-primary pull-right btn-xs">Enroll</a>
                    </td>
                </tr>
                [/#list]
            </tbody>
        </table>
    [/#if]

[/#if]


</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
