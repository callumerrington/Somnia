import platform
import subprocess

# python 2's input() tries to execute the given string?
try:
	_input = raw_input
except:
	_input = input

prop_keys = ['ant', 'forge', 'build_number']
props = {}
try:
	import build_props
	props = build_props.props
except (AttributeError, ImportError):
	pass

for k in prop_keys:
	if not k in props:
		props[k] = _input("Enter missing property value for '%s': " % k)

def main():
	try:
		if platform.system() == "Windows":
			subprocess.call(["cmd", "/c", props['ant'], "-Dforge=%s" % props['forge'], "-Dbuild_number=%s" % props['build_number'], "build_clean"])
		else:
			subprocess.call([props['ant'], "-Dforge=%s" % props['forge'], "-Dbuild_number=%s" % props['build_number'], "build_clean"])
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
