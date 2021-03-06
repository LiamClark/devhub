[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("form.ssh-key-setup.title") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h2>${i18n.translate("form.ssh-key-setup.title")}</h2>
[#if error?? && error?has_content]
			<div class="alert alert-danger">
				${i18n.translate(error)}
			</div>
[/#if]
			<form role="form" method="POST" action="">
				<div class="form-group">
					<label class="control-label" for="key-name">${i18n.translate("form.ssh-key-setup.key-name.label")}</label>
 					<span class="glyphicon form-control-feedback"></span>
					<input type="text" id="key-name" name="name" class="form-control" autofocus="autofocus" placeholder="${i18n.translate("form.ssh-key-setup.key-name.label")}">
					<span class="help-block"></span>
				</div>
				<div class="form-group">
					<label class="control-label" for="key-contents">${i18n.translate("form.ssh-key-setup.key-contents.label")}</label>
					<span class="glyphicon glyphicon-remove form-control-feedback"></span>
					<textarea name="contents" id="key-contents" class="form-control" placeholder="${i18n.translate("form.ssh-key-setup.key-contents.label")}" rows="6"></textarea>
					<span class="help-block"></span>
				</div>
				<div class="form-group">
					<input type="submit" class="btn btn-xl btn-success pull-right" name="add-key" value="${i18n.translate("form.ssh-key-setup.buttons.add-key.caption")}" disabled="disabled">
				</div>
			</form>
		</div>
[@macros.renderScripts /]
		<script type="text/javascript">
			$(document).ready(function() {
				addValidationRule($('input[name="name"]'), /^[a-zA-Z0-9]+$/, '${i18n.translate("error.invalid-key-name")}');
				addValidationRule($('textarea[name="contents"]'), /^ssh-rsa\s.+\s*$/, '${i18n.translate("error.invalid-key-contents")}');
			});
		</script>
[@macros.renderFooter /]
