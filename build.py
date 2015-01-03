import subprocess

prop_keys = ['ant', 'forge', 'build_number']
props = {}
try:
	import build_props
	props = build_props.props
except (AttributeError, ImportError):
	pass

for k in prop_keys:
	try:
		props[k]
	except KeyError:
		props[k] = input("Enter missing property value for '%s': " % k)

def main():
	try:
		subprocess.call(["cmd", "/c", props['ant'], "-Dforge=%s" % props['forge'], "-Dbuild_number=%s" % props['build_number'], "build_clean"])
	except Exception as e:
		print("Error running ant! %s" % e)
	
	props['build_number'] = str(int(props['build_number'])+1)
	write_props(props)

def write_props(props):
	f = open("build_props.py", 'w')
	f.write("props = {\n%s\n\t\t}" % '\n'.join(["\t\t\t\"%s\" : \"%s\"," % (k, v.replace('\\', '\\\\')) for k, v in props.items()]))
	f.close()
	
if __name__ == "__main__":
	main()