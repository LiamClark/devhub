[#macro renderHeader title]
<!DOCTYPE html>
<html>
	<head>
		<title>DevHub - ${title}</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="stylesheet" href="/static/css/devhub.css">
		<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
		<![endif]-->
	</head>
	<body>
[/#macro]

[#macro renderMenu]
		<div class="menu">
			<div class="container">
				<a href="/" class="logo">DEVHUB</a>
				<a href="/" class="btn btn-default pull-right">Sign in</a>
			</div>
		</div>	
[/#macro]

[#macro renderScripts]
		<script src="/static/js/jquery.min.js"></script>
		<script src="/static/js/bootstrap.min.js"></script>
[/#macro]

[#macro renderFooter]
	</body>
</html>
[/#macro]