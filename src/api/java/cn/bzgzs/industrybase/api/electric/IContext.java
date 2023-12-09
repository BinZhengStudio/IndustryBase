package cn.bzgzs.industrybase.api.electric;

import javax.annotation.Nullable;

public interface IContext {
	Context getLastContext();

	@Nullable
	ContextWrapper getLastWrapper();
}
